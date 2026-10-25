
package lv.rtu.clothingsearchapp.service;

import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.model.SearchHistory;
import lv.rtu.clothingsearchapp.repository.SearchHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository repository;

    public SearchHistoryService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public List<SearchHistory> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public SearchHistory save(SearchFilters filters) {
        SearchHistory history = new SearchHistory();
        copyFilters(filters, history);
        return repository.save(history);
    }

    public SearchHistory getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Search not found"));
    }

    @Transactional
    public SearchHistory update(Long id, SearchFilters filters) {
        SearchHistory history = getById(id);
        copyFilters(filters, history);
        return repository.save(history);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void copyFilters(
            SearchFilters filters, SearchHistory history) {
        history.setCategory(clean(filters.getCategory()));
        history.setColor(clean(filters.getColor()));
        history.setBrand(clean(filters.getBrand()));
        history.setMinPrice(filters.getMinPrice());
        history.setMaxPrice(filters.getMaxPrice());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
