package com.campusevents.config;

import com.campusevents.security.CurrentUserArgumentResolver;
import com.campusevents.security.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
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
    private final CorsConfigurationSource corsConfigurationSource;
    
    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver, 
                     JwtAuthFilter jwtAuthFilter,
                     CorsConfigurationSource corsConfigurationSource) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
    
    /**
     * Register CORS filter with highest priority to handle preflight requests.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter(corsConfigurationSource));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
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
