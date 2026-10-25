
package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.ClothingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingItemRepository
        extends JpaRepository<ClothingItem, Long> {
}
