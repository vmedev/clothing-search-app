package lv.rtu.clothingsearchapp.repository;

import lv.rtu.clothingsearchapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Lietotāji: pieteikšanās un reģistrācija. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
