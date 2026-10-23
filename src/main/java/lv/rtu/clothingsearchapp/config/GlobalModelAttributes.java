package lv.rtu.clothingsearchapp.config;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.model.User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Katrā lapā ir pieejami ${currentUser} un ${isGuest} – izmanto galvene (base.html). */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CurrentUser currentUser;

    public GlobalModelAttributes(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return currentUser.get(session).orElse(null);
    }

    @ModelAttribute("isGuest")
    public boolean isGuest(HttpSession session) {
        return currentUser.isGuest(session);
    }
}
