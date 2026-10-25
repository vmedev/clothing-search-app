
package lv.rtu.clothingsearchapp.controller;

import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.service.ClothingSearchService;
import lv.rtu.clothingsearchapp.service.SearchHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SearchController {

    private final ClothingSearchService clothingSearchService;
    private final SearchHistoryService searchHistoryService;

    public SearchController(
            ClothingSearchService clothingSearchService,
            SearchHistoryService searchHistoryService) {
        this.clothingSearchService = clothingSearchService;
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/search";
    }

    @GetMapping("/search")
    public String showSearch(
            @ModelAttribute("filters") SearchFilters filters,
            Model model) {

        model.addAttribute("items", clothingSearchService.search(filters));
        model.addAttribute("categories", List.of(
                "T-Shirts", "Hoodies", "Jackets", "Trousers",
                "Shoes", "Sweatshirts", "Shirts"));
        model.addAttribute("colors", List.of(
                "Black", "White", "Blue", "Beige", "Green"));
        model.addAttribute("brands", List.of(
                "Nike", "Adidas", "Levi's", "Zara", "H&M"));

        return "search";
    }

    @PostMapping("/search")
    public String search(
            @ModelAttribute("filters") SearchFilters filters,
            RedirectAttributes redirectAttributes) {

        if (!validPrices(filters)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please enter valid prices. Minimum must not exceed maximum.");
            return "redirect:/search";
        }

        searchHistoryService.save(filters);

        if (notBlank(filters.getCategory())) {
            redirectAttributes.addAttribute(
                    "category", filters.getCategory());
        }
        if (notBlank(filters.getColor())) {
            redirectAttributes.addAttribute(
                    "color", filters.getColor());
        }
        if (notBlank(filters.getBrand())) {
            redirectAttributes.addAttribute(
                    "brand", filters.getBrand());
        }
        if (filters.getMinPrice() != null) {
            redirectAttributes.addAttribute(
                    "minPrice", filters.getMinPrice());
        }
        if (filters.getMaxPrice() != null) {
            redirectAttributes.addAttribute(
                    "maxPrice", filters.getMaxPrice());
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
}
