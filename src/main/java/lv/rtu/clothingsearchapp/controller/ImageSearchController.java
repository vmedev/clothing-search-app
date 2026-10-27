package lv.rtu.clothingsearchapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.model.Category;
import lv.rtu.clothingsearchapp.model.Product;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.CategoryRepository;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import lv.rtu.clothingsearchapp.service.AiVisionService;
import lv.rtu.clothingsearchapp.service.DetectedClothing;
import lv.rtu.clothingsearchapp.service.SearchHistoryService;
import lv.rtu.clothingsearchapp.service.SimilarProductService;

/**
 * Apģērba meklēšana pēc attēla (prasības 3.1, 3.2, 3.8, 3.9, 3.10).
 * Attēls tiek saglabāts mapē uploads/, MI nosaka kategoriju un krāsu,
 * sistēma parāda līdzīgākās preces un saglabā meklējumu vēsturē (tikai reģistrētam lietotājam).
 * Ja MI nav pieejams, lietotājs kategoriju un krāsu izvēlas pats.
 */
@Controller
public class ImageSearchController {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ImageSearchController.class);

    private final AiVisionService aiVisionService;
    private final SimilarProductService similarProductService;
    private final SearchHistoryService searchHistoryService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ImageSearchController(AiVisionService aiVisionService,
                                 SimilarProductService similarProductService,
                                 SearchHistoryService searchHistoryService,
                                 CategoryRepository categoryRepository,
                                 ProductRepository productRepository) {
        this.aiVisionService = aiVisionService;
        this.similarProductService = similarProductService;
        this.searchHistoryService = searchHistoryService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "search/upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("image") MultipartFile image, HttpSession session, Model model) {
        if (image == null || image.isEmpty()) {
            model.addAttribute("errorMessage", "Lūdzu, izvēlieties attēlu");
            return "search/upload";
        }
        if (!ALLOWED_TYPES.contains(image.getContentType())) {
            model.addAttribute("errorMessage", "Atļauti tikai JPG, PNG vai WEBP attēli");
            return "search/upload";
        }

        String imageUrl;
        try {
            imageUrl = save(image);
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Neizdevās saglabāt attēlu");
            return "search/upload";
        }
        model.addAttribute("imageUrl", imageUrl);

        Optional<DetectedClothing> detected = aiVisionService.analyze(image);
        if (detected.isEmpty()) {
            model.addAttribute("manual", true);
            model.addAttribute("infoMessage",
                    "Neizdevās automātiski atpazīt apģērbu. Izvēlieties kategoriju un krāsu pats.");
            addOptions(model);
            return "search/upload";
        }
        return showResults(detected.get(), imageUrl, session, model);
    }

    /** Rezerves variants, ja MI nav pieejams: kategoriju un krāsu izvēlas lietotājs. */
    @PostMapping("/upload/manual")
    public String manual(@RequestParam String imageUrl,
                         @RequestParam String category,
                         @RequestParam(required = false) String color,
                         HttpSession session, Model model) {
        if (!imageUrl.startsWith("/uploads/")) {
            return "redirect:/upload";
        }
        model.addAttribute("imageUrl", imageUrl);
        String c = (color == null || color.isBlank()) ? null : color;
        return showResults(new DetectedClothing(category, c, ""), imageUrl, session, model);
    }

    private String showResults(DetectedClothing d, String imageUrl, HttpSession session, Model model) {
        List<Product> products = similarProductService.findSimilar(d);

        SearchHistory entry = new SearchHistory();
        entry.setQueryText(d.style() == null || d.style().isBlank()
                ? "Meklēšana pēc attēla"
                : "Attēls: " + d.style());
        entry.setCategory(d.category());
        entry.setColor(d.color());
        entry.setImageUrl(imageUrl);
        boolean saved = searchHistoryService.saveForCurrentUser(session, entry);

        model.addAttribute("detected", d);
        model.addAttribute("products", products);
        model.addAttribute("savedToHistory", saved);
        log.info("[UPLOAD] Atrastas preces: {}, saglabāts vēsturē: {}", products.size(), saved);
        return "search/upload";
    }

    private String save(MultipartFile image) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        String ext = switch (image.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String name = UUID.randomUUID() + ext;
        Files.write(UPLOAD_DIR.resolve(name), image.getBytes());
        return "/uploads/" + name;
    }

    private void addOptions(Model model) {
        model.addAttribute("categories",
                categoryRepository.findAll().stream().map(Category::getName).toList());
        model.addAttribute("colors", productRepository.findDistinctColors());
    }
}