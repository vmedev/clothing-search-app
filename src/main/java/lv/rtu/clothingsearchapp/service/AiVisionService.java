package lv.rtu.clothingsearchapp.service;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
 * Katram modelim ir laika limits; ja modelis neatbild vai ir pārslogots, tiek mēģināts nākamais.
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
                           @Value("${gemini.timeout-seconds:30}") int timeoutSeconds,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.apiKey = apiKey;
        this.models = models;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory)
                .build();

        log.info("AiVisionService: modeļi {}, laika limits {} s, atslēga {}",
                models, timeoutSeconds, apiKey.isBlank() ? "NAV iestatīta" : "iestatīta");
    }

    /**
     * Atgriež atpazīto apģērbu vai Optional.empty(), ja neviens modelis nav pieejams
     * vai atbilde nav derīga (tad lietotājam jārāda manuālā izvēle).
     */
    public Optional<DetectedClothing> analyze(MultipartFile image) {
        long start = System.currentTimeMillis();
        log.info("[MI] Sākta atpazīšana: fails={}, tips={}, izmērs={} KB",
                image.getOriginalFilename(), image.getContentType(), image.getSize() / 1024);

        if (apiKey.isBlank()) {
            log.warn("[MI] GEMINI_API_KEY nav iestatīts, pāreja uz manuālo izvēli");
            return Optional.empty();
        }
        try {
            List<String> categories = categories();
            List<String> colors = colors();
            log.info("[MI] No DB: {} kategorijas, {} krāsas", categories.size(), colors.size());

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
                String m = model.trim();
                long t = System.currentTimeMillis();
                log.info("[MI] Sūta pieprasījumu modelim {} ...", m);
                try {
                    String response = restClient.post()
                            .uri("/models/{model}:generateContent", m)
                            .header("x-goog-api-key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body)
                            .retrieve()
                            .body(String.class);
                    log.info("[MI] Modelis {} atbildēja pēc {} ms", m, System.currentTimeMillis() - t);

                    Optional<DetectedClothing> result = parse(response, categories, colors);
                    log.info("[MI] Kopā {} ms, rezultāts: {}", System.currentTimeMillis() - start,
                            result.map(Object::toString).orElse("neatpazīts"));
                    return result;
                } catch (Exception e) {
                    log.warn("[MI] Modelis {} neizdevās pēc {} ms: {}",
                            m, System.currentTimeMillis() - t, shorten(e.getMessage()));
                }
            }
            log.error("[MI] Neviens modelis nav pieejams, kopā {} ms", System.currentTimeMillis() - start);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[MI] Kļūda: {}", e.getMessage(), e);
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
        log.info("[MI] Atbilde: {}", text);

        JsonNode json = mapper.readTree(text);
        String category = json.path("category").asString(null);
        String color = json.path("color").asString(null);
        String style = json.path("style").asString("");

        if (!categories.contains(category)) {
            log.warn("[MI] Kategorija '{}' nav datubāzē", category);
            return Optional.empty();
        }
        if (!colors.contains(color)) {
            log.info("[MI] Krāsa '{}' nav datubāzē, tiks ignorēta", color);
            color = null;
        }
        return Optional.of(new DetectedClothing(category, color, style));
    }

    /** Saīsina garus kļūdu tekstus, lai žurnāls būtu lasāms. */
    private String shorten(String message) {
        if (message == null) return "bez ziņojuma";
        String oneLine = message.replace("\n", " ");
        return oneLine.length() > 200 ? oneLine.substring(0, 200) + "..." : oneLine;
    }
}