package lv.rtu.clothingsearchapp.controller;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.Favorite;
import lv.rtu.clothingsearchapp.repository.FavoriteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Izlases modulis: apskate un preces noņemšana (projektējums 5.1.6). */
@Controller
public class FavoriteController {

    private final FavoriteRepository favorites;
    private final CurrentUser currentUser;

    public FavoriteController(FavoriteRepository favorites, CurrentUser currentUser) {
        this.favorites = favorites;
        this.currentUser = currentUser;
    }

    @GetMapping("/favorites")
    public String showFavorites(HttpSession session, Model model) {
        List<Favorite> items = currentUser.get(session)
                .map(u -> favorites.findByUserIdOrderByCreatedAtDesc(u.getId()))
                .orElse(List.of());
        model.addAttribute("isGuest", !currentUser.isLoggedIn(session));
        model.addAttribute("favorites", items);
        return "favorites/favorites";
    }

    @PostMapping("/favorites/{productId}/remove")
    @Transactional
    public String remove(@PathVariable Long productId, HttpSession session, RedirectAttributes ra) {
        currentUser.get(session).ifPresent(u -> {
            favorites.deleteByUserIdAndProductId(u.getId(), productId);
            ra.addFlashAttribute("successMessage", "Prece noņemta no izlases");
        });
        return "redirect:/favorites";
    }
}