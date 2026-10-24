package lv.rtu.clothingsearchapp.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lv.rtu.clothingsearchapp.model.Product;
import lv.rtu.clothingsearchapp.repository.ProductRepository;

/**
 * Sakārto preces pēc līdzības ar AI atpazīto apģērbu.
 * Kategorija: 3 punkti, krāsa: 2 punkti, katrs kopīgs vārds aprakstā: 1 punkts.
 */
@Service
public class SimilarProductService {

    private static final int LIMIT = 12;

    private final ProductRepository productRepository;

    public SimilarProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findSimilar(DetectedClothing detected) {
        Set<String> styleWords = words(detected.style());

        return productRepository.findAll().stream()
                .map(p -> new Scored(p, score(p, detected, styleWords)))
                .filter(s -> s.score() > 0)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .limit(LIMIT)
                .map(Scored::product)
                .toList();
    }

    private int score(Product p, DetectedClothing d, Set<String> styleWords) {
        int score = 0;
        if (p.getCategory() != null && p.getCategory().getName().equals(d.category())) {
            score += 3;
        }
        if (d.color() != null && d.color().equals(p.getColor())) {
            score += 2;
        }
        Set<String> descWords = words(p.getDescription());
        for (String w : styleWords) {
            if (descWords.contains(w)) {
                score += 1;
            }
        }
        return score;
    }

    private Set<String> words(String text) {
        if (text == null) {
            return Set.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}]+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());
    }

    private record Scored(Product product, int score) {
    }
}