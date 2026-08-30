package com.eshu.OnlineShopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the React frontend. Only a CorsConfigurationSource
 * bean is exposed here - the actual wiring into the filter chain (via
 * http.cors(...)) happens in SecurityConfig, which is the single owner of
 * the SecurityFilterChain bean. Declaring a second SecurityFilterChain here
 * would conflict with it.
 *
 * Allowed origins are read from the `app.cors.allowed-origins` property
 * (comma separated) so they can be changed per environment without a code
 * change - see application.properties for local dev defaults (Vite's
 * default ports).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // The frontend doesn't rely on cookies (auth is a Bearer token in
        // the Authorization header), so credentials stay disabled - that
        // lets allowed-origins stay simple without needing setAllowCredentials(true).
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
