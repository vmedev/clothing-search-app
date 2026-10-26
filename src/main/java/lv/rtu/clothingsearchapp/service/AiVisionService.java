package lv.rtu.clothingsearchapp.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import lv.rtu.clothingsearchapp.model.Category;
import lv.rtu.clothingsearchapp.repository.CategoryRepository;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Nosūta attēlu uz Gemini API un atgriež atpazīto apģērba kategoriju un krāsu.
 * Atļautās vērtības tiek ņemtas no datubāzes.
 * Ja modelis ir pārslogots, tiek mēģināts nākamais no saraksta gemini.models.
 */
@Service
public class AiVisionService {

    private static final Logger log = LoggerFactory.getLogger(AiVisionService.class);

    private final RestClient restClient;
    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final String apiKey;
    private final List<String> models;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AiVisionService(@Value("${gemini.api-key:}") String apiKey,
                           @Value("${gemini.models}") List<String> models,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.apiKey = apiKey;
        this.models = models;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    /**
     * Atgriež atpazīto apģērbu vai Optional.empty(), ja neviens modelis nav pieejams
     * vai atbilde nav derīga (tad lietotājam jārāda manuālā izvēle).
     */
    public Optional<DetectedClothing> analyze(MultipartFile image) {
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY nav iestatīts");
            return Optional.empty();
        }
        try {
            List<String> categories = categories();
            List<String> colors = colors();

            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(
                            Map.of("text", buildPrompt(categories, colors)),
                            Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64))
                    ))),
                    "generationConfig", Map.of("response_mime_type", "application/json")
            );

            for (String model : models) {
                try {
                    String response = restClient.post()
                            .uri("/models/{model}:generateContent", model.trim())
                            .header("x-goog-api-key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body)
                            .retrieve()
                            .body(String.class);
                    log.info("Gemini modelis: {}", model);
                    return parse(response, categories, colors);
                } catch (Exception e) {
                    log.warn("Modelis {} nav pieejams: {}", model, e.getMessage());
                }
            }
            log.error("Neviens Gemini modelis nav pieejams");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Gemini kļūda: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private List<String> categories() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }

    private List<String> colors() {
        return productRepository.findDistinctColors();
    }

    private String buildPrompt(List<String> categories, List<String> colors) {
        return """
                Nosaki galveno apģērba gabalu attēlā.
                Atbildi tikai ar JSON objektu bez papildu teksta:
                {"category": viena no %s,
                 "color": viena no %s,
                 "style": īss apraksts latviešu valodā 1-3 vārdos}
                Ja precīzas vērtības sarakstā nav, izvēlies tuvāko no saraksta, nevis null.
                Ja attēlā nav apģērba, atgriez {"category": null, "color": null, "style": null}.
                """.formatted(categories, colors);
    }

    private Optional<DetectedClothing> parse(String response, List<String> categories, List<String> colors) {
        JsonNode root = mapper.readTree(response);
        String text = root.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asString("");

        JsonNode json = mapper.readTree(text);
        String category = json.path("category").asString(null);
        String color = json.path("color").asString(null);
        String style = json.path("style").asString("");
        log.info("Gemini atbilde: {}", text);

        if (!categories.contains(category)) {
            return Optional.empty();
        }
        if (!colors.contains(color)) {
            color = null;
        }
        return Optional.of(new DetectedClothing(category, color, style));
    }
}