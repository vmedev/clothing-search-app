package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Apģērbu kategorijas: filtram meklēšanas lapā. */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Atrod kategoriju pēc nosaukuma, ko atpazina MI vai izvēlējās lietotājs. */
    Optional<Category> findByNameIgnoreCase(String name);
}
