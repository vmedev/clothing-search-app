package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Lietotāja izlase. */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /** Izlases lapai: jaunākas preces pirmās. */
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);

}
