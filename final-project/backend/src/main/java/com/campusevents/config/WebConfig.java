package com.campusevents.config;

import com.campusevents.security.CurrentUserArgumentResolver;
import com.campusevents.security.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration.
 * 
 * Configures:
 * - JWT authentication filter
 * - Custom argument resolvers (@CurrentUser)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final JwtAuthFilter jwtAuthFilter;
    
    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver, JwtAuthFilter jwtAuthFilter) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.jwtAuthFilter = jwtAuthFilter;
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
    
    /**
     * Register JWT filter to apply to all requests.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
