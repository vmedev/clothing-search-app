package lv.rtu.clothingsearchapp.controller;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.model.Category;
import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.CategoryRepository;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import lv.rtu.clothingsearchapp.service.ClothingSearchService;
import lv.rtu.clothingsearchapp.service.SearchHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Preču meklēšana pēc parametriem (projektējums 5.1.4). */
@Controller
public class SearchController {

    private final ClothingSearchService clothingSearchService;
    private final SearchHistoryService searchHistoryService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public SearchController(ClothingSearchService clothingSearchService,
                            SearchHistoryService searchHistoryService,
                            CategoryRepository categoryRepository,
                            ProductRepository productRepository) {
        this.clothingSearchService = clothingSearchService;
        this.searchHistoryService = searchHistoryService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/search";
    }

    @GetMapping("/search")
    public String showSearch(@ModelAttribute("filters") SearchFilters filters, Model model) {
        model.addAttribute("items", clothingSearchService.search(filters));
        model.addAttribute("categories",
                categoryRepository.findAll().stream().map(Category::getName).toList());
        model.addAttribute("colors", productRepository.findDistinctColors());
        model.addAttribute("brands", productRepository.findDistinctBrands());
        return "search/search";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute("filters") SearchFilters filters,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        if (!validPrices(filters)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ievadiet derīgas cenas. Minimālā cena nedrīkst pārsniegt maksimālo.");
            return "redirect:/search";
        }

        SearchHistory entry = new SearchHistory();
        entry.setQueryText(SearchHistoryService.describe(filters.getCategory(), filters.getColor(),
                filters.getBrand(), filters.getMinPrice(), filters.getMaxPrice()));
        entry.setCategory(blankToNull(filters.getCategory()));
        entry.setColor(blankToNull(filters.getColor()));
        entry.setBrand(blankToNull(filters.getBrand()));
        entry.setMinPrice(filters.getMinPrice());
        entry.setMaxPrice(filters.getMaxPrice());
        searchHistoryService.saveForCurrentUser(session, entry);

        if (notBlank(filters.getCategory())) {
            redirectAttributes.addAttribute("category", filters.getCategory());
        }
        if (notBlank(filters.getColor())) {
            redirectAttributes.addAttribute("color", filters.getColor());
        }
        if (notBlank(filters.getBrand())) {
            redirectAttributes.addAttribute("brand", filters.getBrand());
        }
        if (filters.getMinPrice() != null) {
            redirectAttributes.addAttribute("minPrice", filters.getMinPrice());
        }
        if (filters.getMaxPrice() != null) {
            redirectAttributes.addAttribute("maxPrice", filters.getMaxPrice());
        }
        return "redirect:/search";
    }

    private boolean validPrices(SearchFilters filters) {
        BigDecimal min = filters.getMinPrice();
        BigDecimal max = filters.getMaxPrice();
        return (min == null || min.signum() >= 0)
                && (max == null || max.signum() >= 0)
                && (min == null || max == null || min.compareTo(max) <= 0);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        return notBlank(value) ? value : null;
    }
}