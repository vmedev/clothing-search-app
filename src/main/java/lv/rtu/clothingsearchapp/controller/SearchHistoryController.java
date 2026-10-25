
package lv.rtu.clothingsearchapp.controller;

import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.service.SearchHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(
            SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("/history")
    public String showHistory(Model model) {
        model.addAttribute(
                "historyItems", searchHistoryService.getAll());
        return "history";
    }

    @GetMapping("/history/{id}/edit")
    public String editHistory(
            @PathVariable Long id, Model model) {
        SearchHistory history = searchHistoryService.getById(id);

        SearchFilters filters = new SearchFilters();
        filters.setCategory(history.getCategory());
        filters.setColor(history.getColor());
        filters.setBrand(history.getBrand());
        filters.setMinPrice(history.getMinPrice());
        filters.setMaxPrice(history.getMaxPrice());

        model.addAttribute("historyItem", history);
        model.addAttribute("filters", filters);
        addOptions(model);

        return "edit-history";
    }

    @PostMapping("/history/{id}/edit")
    public String updateHistory(
            @PathVariable Long id,
            @ModelAttribute("filters") SearchFilters filters,
            RedirectAttributes redirectAttributes) {

        if (!validPrices(filters)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please enter valid prices. Minimum must not exceed maximum.");
            return "redirect:/history/" + id + "/edit";
        }

        searchHistoryService.update(id, filters);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Search updated successfully.");

        return "redirect:/history";
    }

    @PostMapping("/history/{id}/delete")
    public String deleteHistory(@PathVariable Long id) {
        searchHistoryService.delete(id);
        return "redirect:/history";
    }

    @GetMapping("/history/{id}/repeat")
    public String repeatHistory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        SearchHistory history = searchHistoryService.getById(id);

        addIfPresent(redirectAttributes, "category", history.getCategory());
        addIfPresent(redirectAttributes, "color", history.getColor());
        addIfPresent(redirectAttributes, "brand", history.getBrand());

        if (history.getMinPrice() != null) {
            redirectAttributes.addAttribute(
                    "minPrice", history.getMinPrice());
        }
        if (history.getMaxPrice() != null) {
            redirectAttributes.addAttribute(
                    "maxPrice", history.getMaxPrice());
        }

        return "redirect:/search";
    }

    private void addIfPresent(
            RedirectAttributes attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.addAttribute(key, value);
        }
    }

    private void addOptions(Model model) {
        model.addAttribute("categories", List.of(
                "T-Shirts", "Hoodies", "Jackets", "Trousers",
                "Shoes", "Sweatshirts", "Shirts"));
        model.addAttribute("colors", List.of(
                "Black", "White", "Blue", "Beige", "Green"));
        model.addAttribute("brands", List.of(
                "Nike", "Adidas", "Levi's", "Zara", "H&M"));
    }

    private boolean validPrices(SearchFilters filters) {
        BigDecimal min = filters.getMinPrice();
        BigDecimal max = filters.getMaxPrice();

        return (min == null || min.signum() >= 0)
                && (max == null || max.signum() >= 0)
                && (min == null || max == null || min.compareTo(max) <= 0);
    }
}
