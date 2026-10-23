package lv.rtu.clothingsearchapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Ja cilvēks nav ne ienācis, ne izvēlējies viesa režīmu, sūtām uz pieteikšanās lapu.
 * Pieteikšanās/reģistrācijas lapas un statiskie faili ir izņēmumi (sk. WebConfig).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final CurrentUser currentUser;

    public AuthInterceptor(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        var session = req.getSession();
        if (currentUser.isLoggedIn(session) || currentUser.isGuest(session)) {
            return true;
        }
        res.sendRedirect(req.getContextPath() + "/login");
        return false;
    }
}
