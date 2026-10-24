package lv.rtu.clothingsearchapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LoginRequiredInterceptor loginRequiredInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, LoginRequiredInterceptor loginRequiredInterceptor) {
        this.authInterceptor = authInterceptor;
        this.loginRequiredInterceptor = loginRequiredInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/guest", "/test/**",
                        "/style.css", "/css/**", "/img/**", "/js/**", "/uploads/**", "/favicon.ico", "/error", "/h2-console/**");

        // Viesim nav vēstures, izlases un profila
        registry.addInterceptor(loginRequiredInterceptor)
                .addPathPatterns("/history/**", "/favorites/**", "/profile/**");
    }
}
