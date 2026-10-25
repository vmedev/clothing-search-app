
package lv.rtu.clothingsearchapp.service;

import lv.rtu.clothingsearchapp.model.ClothingItem;
import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.repository.ClothingItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ClothingSearchService {

    private final ClothingItemRepository repository;

    public ClothingSearchService(ClothingItemRepository repository) {
        this.repository = repository;
    }

    public List<ClothingItem> search(SearchFilters filters) {
        return repository.findAll().stream()
                .filter(item -> matches(
                        item.getCategory(), filters.getCategory()))
                .filter(item -> matches(
                        item.getColor(), filters.getColor()))
                .filter(item -> matches(
                        item.getBrand(), filters.getBrand()))
                .filter(item -> filters.getMinPrice() == null
                        || (item.getPrice() != null
                        && item.getPrice().compareTo(
                        filters.getMinPrice()) >= 0))
                .filter(item -> filters.getMaxPrice() == null
                        || (item.getPrice() != null
                        && item.getPrice().compareTo(
                        filters.getMaxPrice()) <= 0))
                .toList();
    }

    private boolean matches(String value, String filter) {
        return filter == null
                || filter.isBlank()
                || (value != null && value.toLowerCase(Locale.ROOT)
                .equals(filter.toLowerCase(Locale.ROOT)));
    }
}
