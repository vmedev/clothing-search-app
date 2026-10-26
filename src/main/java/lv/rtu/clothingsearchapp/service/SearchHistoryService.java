package lv.rtu.clothingsearchapp.service;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Meklēšanas vēsture (prasības 3.4, 3.5, 3.6, 3.11).
 * Vēsture tiek saglabāta un rādīta TIKAI pieteikušamies lietotājam, viesim nekad.
 * Katrs lietotājs redz, labo un dzēš tikai savus ierakstus.
 */
@Service
public class SearchHistoryService {

    private final SearchHistoryRepository history;
    private final CurrentUser currentUser;

    public SearchHistoryService(SearchHistoryRepository history, CurrentUser currentUser) {
        this.history = history;
        this.currentUser = currentUser;
    }

    /** @return true, ja saglabāts; false, ja viesis */
    public boolean saveForCurrentUser(HttpSession session, SearchHistory entry) {
        return currentUser.get(session)
                .map(user -> {
                    entry.setUser(user);
                    history.save(entry);
                    return true;
                })
                .orElse(false);
    }

    public List<SearchHistory> getForCurrentUser(HttpSession session) {
        return currentUser.get(session)
                .map(u -> history.findByUserIdOrderByCreatedAtDesc(u.getId()))
                .orElse(List.of());
    }

    public Optional<SearchHistory> findOwn(HttpSession session, Long id) {
        return currentUser.get(session)
                .flatMap(u -> history.findByIdAndUserId(id, u.getId()));
    }

    public boolean update(HttpSession session, Long id, SearchFilters f) {
        return findOwn(session, id).map(entry -> {
            entry.setCategory(blankToNull(f.getCategory()));
            entry.setColor(blankToNull(f.getColor()));
            entry.setBrand(blankToNull(f.getBrand()));
            entry.setMinPrice(f.getMinPrice());
            entry.setMaxPrice(f.getMaxPrice());
            entry.setQueryText(describe(f.getCategory(), f.getColor(), f.getBrand(),
                    f.getMinPrice(), f.getMaxPrice()));
            history.save(entry);
            return true;
        }).orElse(false);
    }

    public boolean delete(HttpSession session, Long id) {
        return findOwn(session, id).map(entry -> {
            history.delete(entry);
            return true;
        }).orElse(false);
    }

    /** Īss apraksts, piemēram "Bikses, melna, Zara, līdz 50 €". */
    public static String describe(String category, String color, String brand,
                                  BigDecimal min, BigDecimal max) {
        List<String> parts = new ArrayList<>();
        if (notBlank(category)) parts.add(category);
        if (notBlank(color)) parts.add(color);
        if (notBlank(brand)) parts.add(brand);
        if (min != null) parts.add("no " + min + " €");
        if (max != null) parts.add("līdz " + max + " €");
        return parts.isEmpty() ? "Visas preces" : String.join(", ", parts);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String blankToNull(String s) {
        return notBlank(s) ? s : null;
    }
}