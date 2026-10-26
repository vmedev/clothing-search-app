package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** Apģērbu katalogs. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p.color FROM Product p WHERE p.color IS NOT NULL ORDER BY p.color")
    List<String> findDistinctColors();

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
}