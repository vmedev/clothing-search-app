package lv.rtu.clothingsearchapp.controller;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.User;
import lv.rtu.clothingsearchapp.service.AuthService;
import lv.rtu.clothingsearchapp.service.AuthService.AuthException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Pieteikšanās, reģistrācija, viesa režīms un izrakstīšanās. */
@Controller
public class AuthController {

    private final AuthService auth;
    private final CurrentUser currentUser;

    public AuthController(AuthService auth, CurrentUser currentUser) {
        this.auth = auth;
        this.currentUser = currentUser;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (currentUser.isLoggedIn(session)) return "redirect:/";
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        try {
            User u = auth.login(username, password);
            currentUser.login(session, u);
            return "redirect:/";
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
                           @RequestParam String password, @RequestParam String passwordRepeat,
                           HttpSession session, Model model, RedirectAttributes ra) {
        try {
            if (!password.equals(passwordRepeat)) {
                throw new AuthException("Paroles nesakrīt");
            }
            User u = auth.register(username, email, password);
            currentUser.login(session, u);
            ra.addFlashAttribute("success", "Konts izveidots! Laipni lūgti, " + u.getUsername() + ".");
            return "redirect:/";
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    /** Viesa režīms: var meklēt un skatīt preces, bet vēsture un izlase netiek saglabātas. */
    @GetMapping("/guest")
    public String guest(HttpSession session) {
        currentUser.enterAsGuest(session);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
