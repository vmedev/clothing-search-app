package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Apģērbu kategorijas: filtram. */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);
}
