package lv.rtu.clothingsearchapp.config;

import lv.rtu.clothingsearchapp.model.Role;
import lv.rtu.clothingsearchapp.model.User;
import lv.rtu.clothingsearchapp.repository.UserRepository;
import lv.rtu.clothingsearchapp.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Izveido demo kontus prototipa demonstrācijai (paroles tiek hešotas ar BCrypt). */
@Component
public class DemoUsersInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final AuthService auth;

    public DemoUsersInitializer(UserRepository users, AuthService auth) {
        this.users = users;
        this.auth = auth;
    }

    @Override
    public void run(String... args) {
        create("demo", "demo@apgerbi.lv", "demo1234", Role.USER);
        create("admin", "admin@apgerbi.lv", "admin1234", Role.ADMIN);
    }

    private void create(String username, String email, String password, Role role) {
        if (users.existsByUsernameIgnoreCase(username)) return;
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(auth.hash(password));
        u.setRole(role);
        users.save(u);
    }
}
