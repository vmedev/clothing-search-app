package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Meklēšanas vēsture. */
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /** Vēstures lapai: jaunākie meklējumi pirmie. */
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Rediģēšanai un dzēšanai: atrod ierakstu tikai tad, ja tas pieder šim lietotājam. */
    Optional<SearchHistory> findByIdAndUserId(Long id, Long userId);
}
