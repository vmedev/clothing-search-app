package lv.rtu.clothingsearchapp.service;

import lv.rtu.clothingsearchapp.model.User;
import lv.rtu.clothingsearchapp.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Autorizācijas modulis: reģistrācija (projektējums 5.1.1) un autorizācija (5.1.2).
 * Kļūdas gadījumā met AuthException ar paziņojumu latviski.
 */
@Service
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User register(String username, String email, String password) {
        if (isBlank(username) || isBlank(email) || isBlank(password)) {
            throw new AuthException("Ir jāievada visi dati");
        }
        if (!username.trim().matches("[A-Za-z0-9_.]{3,30}")) {
            throw new AuthException("Lietotājvārdā drīkst būt 3–30 latīņu burti, cipari, _ vai .");
        }
        if (users.existsByUsernameIgnoreCase(username.trim())) {
            throw new AuthException("Šāds lietotājvārds jau ir aizņemts");
        }
        if (users.existsByEmailIgnoreCase(email.trim())) {
            throw new AuthException("Lietotājs ar tādu e-pastu jau ir datubāzē");
        }
        if (!isPasswordValid(password)) {
            throw new AuthException("Parole neatbilst prasībām (vismaz 8 simboli, vismaz 1 cipars)");
        }
        User u = new User();
        u.setUsername(username.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(PasswordHasher.hash(password));
        return users.save(u);
    }

    public User login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new AuthException("Lietotājvārds vai parole nav pareizi");
        }
        User u = users.findByUsernameIgnoreCase(username.trim())
                .filter(found -> PasswordHasher.matches(password, found.getPasswordHash()))
                .orElseThrow(() -> new AuthException("Lietotājvārds vai parole nav pareizi"));
        if (u.isBlocked()) {
            throw new AuthException("Konts ir bloķēts");
        }
        return u;
    }

    public String hash(String rawPassword) {
        return PasswordHasher.hash(rawPassword);
    }

    static boolean isPasswordValid(String p) {
        return p.length() >= 8 && p.chars().anyMatch(Character::isDigit);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) { super(message); }
    }
}
