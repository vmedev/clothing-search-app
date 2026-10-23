package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/** Apģērbu katalogs. */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
