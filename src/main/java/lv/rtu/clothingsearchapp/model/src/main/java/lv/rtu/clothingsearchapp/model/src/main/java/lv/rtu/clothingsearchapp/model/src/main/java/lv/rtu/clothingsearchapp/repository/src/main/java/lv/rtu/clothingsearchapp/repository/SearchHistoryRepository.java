
package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository
        extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findAllByOrderByCreatedAtDesc();
}
