package lv.rtu.clothingsearchapp.service;

import lv.rtu.clothingsearchapp.model.Product;
import lv.rtu.clothingsearchapp.model.SearchFilters;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ClothingSearchService {

    private final ProductRepository repository;

    public ClothingSearchService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> search(SearchFilters filters) {
        return repository.findAll().stream()
                .filter(p -> matches(
                        p.getCategory() != null ? p.getCategory().getName() : null,
                        filters.getCategory()))
                .filter(p -> matches(p.getColor(), filters.getColor()))
                .filter(p -> matches(p.getBrand(), filters.getBrand()))
                .filter(p -> filters.getMinPrice() == null
                        || (p.getPrice() != null
                        && p.getPrice().compareTo(filters.getMinPrice()) >= 0))
                .filter(p -> filters.getMaxPrice() == null
                        || (p.getPrice() != null
                        && p.getPrice().compareTo(filters.getMaxPrice()) <= 0))
                .toList();
    }

    private boolean matches(String value, String filter) {
        return filter == null
                || filter.isBlank()
                || (value != null && value.toLowerCase(Locale.ROOT)
                .equals(filter.toLowerCase(Locale.ROOT)));
    }
}