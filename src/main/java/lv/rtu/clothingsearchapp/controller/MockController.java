package lv.rtu.clothingsearchapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lv.rtu.clothingsearchapp.repository.ProductRepository;

/**
 * Prototipa lapas ar iekodētiem datiem (paziņojumi, profils, administrators).
 * Funkcionalitāte nav realizēta, lapas parāda, kā sistēmai būtu jāizskatās.
 */
@Controller
public class MockController {

    private final ProductRepository productRepository;

    public MockController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "mock/notifications";
    }

    @GetMapping("/profile")
    public String profile() {
        return "mock/profile";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "mock/admin";
    }
}