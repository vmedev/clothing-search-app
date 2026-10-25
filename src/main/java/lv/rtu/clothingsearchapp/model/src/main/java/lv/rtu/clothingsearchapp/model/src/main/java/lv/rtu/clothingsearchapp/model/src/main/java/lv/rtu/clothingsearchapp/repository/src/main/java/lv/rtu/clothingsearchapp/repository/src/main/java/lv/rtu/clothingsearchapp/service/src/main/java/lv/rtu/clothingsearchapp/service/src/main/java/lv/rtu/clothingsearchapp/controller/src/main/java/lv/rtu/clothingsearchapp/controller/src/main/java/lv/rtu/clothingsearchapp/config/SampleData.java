
package lv.rtu.clothingsearchapp.config;

import lv.rtu.clothingsearchapp.model.ClothingItem;
import lv.rtu.clothingsearchapp.repository.ClothingItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class SampleData {

    @Bean
    CommandLineRunner loadSampleData(
            ClothingItemRepository repository) {

        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.saveAll(List.of(
                new ClothingItem(
                    "Classic White T-Shirt", "T-Shirts", "White",
                    "Nike", new BigDecimal("24.99"),
                    "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600"
                ),
                new ClothingItem(
                    "Black Hoodie", "Hoodies", "Black",
                    "Adidas", new BigDecimal("49.99"),
                    "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600"
                ),
                new ClothingItem(
                    "Blue Denim Jacket", "Jackets", "Blue",
                    "Levi's", new BigDecimal("79.99"),
                    "https://images.unsplash.com/photo-1543076447-215ad9ba6923?w=600"
                ),
                new ClothingItem(
                    "Beige Trousers", "Trousers", "Beige",
                    "Zara", new BigDecimal("39.99"),
                    "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600"
                ),
                new ClothingItem(
                    "White Sneakers", "Shoes", "White",
                    "Adidas", new BigDecimal("69.99"),
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"
                ),
                new ClothingItem(
                    "Black Running Shoes", "Shoes", "Black",
                    "Nike", new BigDecimal("89.99"),
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"
                ),
                new ClothingItem(
                    "Green Sweatshirt", "Sweatshirts", "Green",
                    "H&M", new BigDecimal("34.99"),
                    "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600"
                ),
                new ClothingItem(
                    "Striped Shirt", "Shirts", "White",
                    "Zara", new BigDecimal("29.99"),
                    "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600"
                )
            ));
        };
    }
}
