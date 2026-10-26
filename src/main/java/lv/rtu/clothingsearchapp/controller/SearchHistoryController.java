package lv.rtu.clothingsearchapp.controller;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.Category;
import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.CategoryRepository;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import lv.rtu.clothingsearchapp.service.SearchHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Meklēšanas vēstures apskate, labošana un dzēšana (prasības 3.4, 3.5). */
@Controller
public class SearchHistoryController {

    private static final String NOT_FOUND = "Ieraksts nav atrasts";

    private final SearchHistoryService searchHistoryService;
    private final CurrentUser currentUser;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public SearchHistoryController(SearchHistoryService searchHistoryService,
                                   CurrentUser currentUser,
                                   CategoryRepository categoryRepository,
                                   ProductRepository productRepository) {
        this.searchHistoryService = searchHistoryService;
        this.currentUser = currentUser;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        model.addAttribute("isGuest", !currentUser.isLoggedIn(session));
        model.addAttribute("historyItems", searchHistoryService.getForCurrentUser(session));
        return "history/history";
    }

    @GetMapping("/history/{id}/edit")
    public String editHistory(@PathVariable Long id, HttpSession session,
                              Model model, RedirectAttributes ra) {
        var found = searchHistoryService.findOwn(session, id);
        if (found.isEmpty()) {
            ra.addFlashAttribute("errorMessage", NOT_FOUND);
            return "redirect:/history";
        }
        SearchHistory h = found.get();

        SearchFilters filters = new SearchFilters();
        filters.setCategory(h.getCategory());
        filters.setColor(h.getColor());
        filters.setBrand(h.getBrand());
        filters.setMinPrice(h.getMinPrice());
        filters.setMaxPrice(h.getMaxPrice());

        model.addAttribute("historyItem", h);
        model.addAttribute("filters", filters);
        addOptions(model);
        return "history/edit-history";
    }

    @PostMapping("/history/{id}/edit")
    public String updateHistory(@PathVariable Long id,
                                @ModelAttribute("filters") SearchFilters filters,
                                HttpSession session, RedirectAttributes ra) {
        if (!validPrices(filters)) {
            ra.addFlashAttribute("errorMessage",
                    "Ievadiet derīgas cenas. Minimālā cena nedrīkst pārsniegt maksimālo.");
            return "redirect:/history/" + id + "/edit";
        }
        if (searchHistoryService.update(session, id, filters)) {
            ra.addFlashAttribute("successMessage", "Ieraksts atjaunots");
        } else {
            ra.addFlashAttribute("errorMessage", NOT_FOUND);
        }
        return "redirect:/history";
    }

    @PostMapping("/history/{id}/delete")
    public String deleteHistory(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (searchHistoryService.delete(session, id)) {
            ra.addFlashAttribute("successMessage", "Ieraksts dzēsts");
        } else {
            ra.addFlashAttribute("errorMessage", NOT_FOUND);
        }
        return "redirect:/history";
    }

    @GetMapping("/history/{id}/repeat")
    public String repeatHistory(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        var found = searchHistoryService.findOwn(session, id);
        if (found.isEmpty()) {
            ra.addFlashAttribute("errorMessage", NOT_FOUND);
            return "redirect:/history";
        }
        SearchHistory h = found.get();
        addIfPresent(ra, "category", h.getCategory());
        addIfPresent(ra, "color", h.getColor());
        addIfPresent(ra, "brand", h.getBrand());
        if (h.getMinPrice() != null) ra.addAttribute("minPrice", h.getMinPrice());
        if (h.getMaxPrice() != null) ra.addAttribute("maxPrice", h.getMaxPrice());
        return "redirect:/search";
    }

    private void addIfPresent(RedirectAttributes ra, String key, String value) {
        if (value != null && !value.isBlank()) {
            ra.addAttribute(key, value);
        }
    }

    private void addOptions(Model model) {
        model.addAttribute("categories",
                categoryRepository.findAll().stream().map(Category::getName).toList());
        model.addAttribute("colors", productRepository.findDistinctColors());
        model.addAttribute("brands", productRepository.findDistinctBrands());
    }

    private boolean validPrices(SearchFilters f) {
        BigDecimal min = f.getMinPrice();
        BigDecimal max = f.getMaxPrice();
        return (min == null || min.signum() >= 0)
                && (max == null || max.signum() >= 0)
                && (min == null || max == null || min.compareTo(max) <= 0);
    }
}