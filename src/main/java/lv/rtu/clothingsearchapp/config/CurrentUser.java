package lv.rtu.clothingsearchapp.config;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.model.User;
import lv.rtu.clothingsearchapp.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Palīgklase darbam ar sesiju: kurš lietotājs ir ienācis vai tas ir viesis.
 * Lieto citos kontrolieros: currentUser.get(session).ifPresent(...)
 */
@Component
public class CurrentUser {

    public static final String USER_ID = "userId";
    public static final String GUEST = "guest";

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public Optional<User> get(HttpSession session) {
        Object id = session.getAttribute(USER_ID);
        if (!(id instanceof Long userId)) return Optional.empty();
        return users.findById(userId).filter(u -> !u.isBlocked());
    }

    public boolean isLoggedIn(HttpSession session) {
        return get(session).isPresent();
    }

    public boolean isGuest(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(GUEST));
    }

    public void login(HttpSession session, User user) {
        session.removeAttribute(GUEST);
        session.setAttribute(USER_ID, user.getId());
    }

    public void enterAsGuest(HttpSession session) {
        session.removeAttribute(USER_ID);
        session.setAttribute(GUEST, true);
    }
}
