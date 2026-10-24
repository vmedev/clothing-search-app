package lv.rtu.clothingsearchapp.service;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

/**
 * Meklēšanas vēstures saglabāšana ar viesa režīma noteikumu:
 * vēsture tiek saglabāta TIKAI pieteikušamies lietotājam, viesim – nekad.
 *
 * Lietošana meklēšanas kontrolierī (8. un 9. punkts):
 *
 *   SearchHistory entry = new SearchHistory();
 *   entry.setQueryText("Brūns T-krekls");
 *   entry.setCategory(...); entry.setColor(...); entry.setImageUrl(...);
 *   searchHistoryService.saveForCurrentUser(session, entry);
 */
@Service
public class SearchHistoryService {

    private final SearchHistoryRepository history;
    private final CurrentUser currentUser;

    public SearchHistoryService(SearchHistoryRepository history, CurrentUser currentUser) {
        this.history = history;
        this.currentUser = currentUser;
    }

    /**
     * Saglabā ierakstu vēsturē, ja lietotājs ir pieteicies.
     * @return true – saglabāts; false – viesis, nekas netika saglabāts
     */
    public boolean saveForCurrentUser(HttpSession session, SearchHistory entry) {
        return currentUser.get(session)
                .map(user -> {
                    entry.setUser(user);
                    history.save(entry);
                    return true;
                })
                .orElse(false);
    }
}
