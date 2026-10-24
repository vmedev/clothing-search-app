package lv.rtu.clothingsearchapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Vēsture, izlase un profils pieejami tikai pieteikušamies lietotājam.
 * Viesis tiek novirzīts uz pieteikšanās lapu ar paskaidrojumu.
 * Ceļi ir norādīti WebConfig.
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    private final CurrentUser currentUser;

    public LoginRequiredInterceptor(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (currentUser.isLoggedIn(req.getSession())) {
            return true;
        }
        res.sendRedirect(req.getContextPath() + "/login?required");
        return false;
    }
}
