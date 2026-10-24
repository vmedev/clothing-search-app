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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


@Service
public class AiVisionService {

    private static final Logger log = LoggerFactory.getLogger(AiVisionService.class);

    public static final List<String> CATEGORIES =
            List.of("Jakas", "Krekli", "Bikses", "Kleitas", "Džemperi", "Svārki");
    public static final List<String> COLORS =
            List.of("melna", "balta", "zila", "sarkana", "zaļa", "pelēka", "brūna", "bēša", "rozā", "dzeltena");

    private static final String PROMPT = """
            Nosaki galveno apģērba gabalu attēlā.
            Atbildi tikai ar JSON objektu bez papildu teksta:
            {"category": viena no %s,
             "color": viena no %s,
             "style": īss apraksts latviešu valodā 1-3 vārdos}
            Ja attēlā nav apģērba, atgriez {"category": null, "color": null, "style": null}.
            """.formatted(CATEGORIES, COLORS);

    private final RestClient restClient;
    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final String apiKey;
    private final String model;

    public AiVisionService(@Value("${gemini.api-key:}") String apiKey,
                           @Value("${gemini.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public Optional<DetectedClothing> analyze(MultipartFile image) {
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY nav iestatīts");
            return Optional.empty();
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(
                            Map.of("text", PROMPT),
                            Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64))
                    ))),
                    "generationConfig", Map.of("response_mime_type", "application/json")
            );

            String response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parse(response);
        } catch (Exception e) {
            log.error("Gemini kļūda: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<DetectedClothing> parse(String response) {
        JsonNode root = mapper.readTree(response);
        String text = root.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asString("");

        JsonNode json = mapper.readTree(text);
        String category = json.path("category").asString(null);
        String color = json.path("color").asString(null);
        String style = json.path("style").asString("");

        if (!CATEGORIES.contains(category)) {
            return Optional.empty();
        }
        if (!COLORS.contains(color)) {
            color = null;
        }
        return Optional.of(new DetectedClothing(category, color, style));
    }
}