package com.eshu.OnlineShopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Deliberately its own tiny config class, separate from SecurityConfig.
 *
 * SecurityConfig field-injects SellerAccessInterceptor, which needs
 * AuthenticatedAccountResolver, which needs UserService, which needs a
 * PasswordEncoder. If that PasswordEncoder bean lived inside SecurityConfig
 * (as it originally did), Spring can't finish constructing SecurityConfig
 * without first calling one of its own @Bean methods - a real cycle, and
 * Spring Boot refuses to start rather than guess how to break it. Defining
 * it here instead means PasswordEncoder never depends on SecurityConfig
 * existing at all.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
