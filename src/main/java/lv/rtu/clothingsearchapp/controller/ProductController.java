package lv.rtu.clothingsearchapp.controller;

import jakarta.servlet.http.HttpSession;
import lv.rtu.clothingsearchapp.config.CurrentUser;
import lv.rtu.clothingsearchapp.model.Favorite;
import lv.rtu.clothingsearchapp.model.Product;
import lv.rtu.clothingsearchapp.model.User;
import lv.rtu.clothingsearchapp.repository.FavoriteRepository;
import lv.rtu.clothingsearchapp.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Preces pārskata apakšmodulis (projektējums 5.1.5):
 * preces kartīte, "Pievienot izlasei" un pāreja uz veikalu.
 *
 * Dati nāk no tabulas PRODUCT (data.sql):
 * ID, IMAGE_NAME, COLOR, BRAND, PRICE, IMAGE_URL (saite uz veikalu), DESCRIPTION.
 */
@Controller
public class ProductController {

    /** Veikala nosaukums pēc saites domēna (tabulā nav atsevišķas veikalu kolonnas). */
    private static final Map<String, String> STORES = Map.of(
            "aboutyou.lv", "About You",
            "cropp.com", "Cropp",
            "housebrand.com", "House",
            "pullandbear.com", "Pull&Bear",
            "reserved.com", "Reserved",
            "sinsay.com", "Sinsay",
            "zalando.lv", "Zalando",
            "zara.com", "Zara",
            "hm.com", "H&M");

    private final ProductRepository products;
    private final FavoriteRepository favorites;
    private final CurrentUser currentUser;

    public ProductController(ProductRepository products, FavoriteRepository favorites, CurrentUser currentUser) {
        this.products = products;
        this.favorites = favorites;
        this.currentUser = currentUser;
    }

    @GetMapping("/product/{id}")
    public String productCard(@PathVariable Long id, HttpSession session, Model model) {
        Product p = findOr404(id);
        model.addAttribute("product", p);
        model.addAttribute("storeName", storeName(p.getImageUrl()));
        boolean inFavorites = currentUser.get(session)
                .map(u -> favorites.existsByUserIdAndProductId(u.getId(), p.getId()))
                .orElse(false);
        model.addAttribute("inFavorites", inFavorites);
        return "product/card";
    }

    @PostMapping("/product/{id}/favorite")
    public String addToFavorites(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Product p = findOr404(id);
        Optional<User> user = currentUser.get(session);

        if (user.isEmpty()) {
            ra.addFlashAttribute("error", "Lai pievienotu preci izlasei, jāautorizējas");
        } else if (favorites.existsByUserIdAndProductId(user.get().getId(), p.getId())) {
            ra.addFlashAttribute("info", "Prece jau ir izlasē");
        } else {
            favorites.save(new Favorite(user.get(), p));
            ra.addFlashAttribute("success", "Prece pievienota izlasei");
        }
        return "redirect:/product/" + id;
    }

    @PostMapping("/product/{id}/favorite/remove")
    public String removeFromFavorites(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        currentUser.get(session).ifPresent(u -> {
            favorites.deleteByUserIdAndProductId(u.getId(), id);
            ra.addFlashAttribute("success", "Prece noņemta no izlases");
        });
        return "redirect:/product/" + id;
    }

    /** Novirza uz preces lapu veikalā (pirkums notiek veikala mājaslapā). */
    @GetMapping("/product/{id}/store")
    public String goToStore(@PathVariable Long id) {
        String url = findOr404(id).getImageUrl();
        if (url == null || !url.startsWith("https://")) {
            return "redirect:/product/" + id;
        }
        return "redirect:" + url;
    }

    static String storeName(String url) {
        if (url == null) return "—";
        try {
            String host = URI.create(url).getHost();
            if (host == null) return "—";
            for (var e : STORES.entrySet()) {
                if (host.endsWith(e.getKey())) return e.getValue();
            }
            return host.replaceFirst("^www\\d*\\.", "");
        } catch (IllegalArgumentException e) {
            return "—";
        }
    }

    private Product findOr404(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prece nav atrasta"));
    }
}
