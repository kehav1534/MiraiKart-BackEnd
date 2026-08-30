package com.eshu.OnlineShopping.config;

import com.eshu.OnlineShopping.security.JwtAuthFilter;
import com.eshu.OnlineShopping.security.SellerAccessInterceptor;
import com.eshu.OnlineShopping.service.SellerDetailsService;
import com.eshu.OnlineShopping.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Role-based, stateless security configuration.
 *
 * Two account types exist - USER (buyers) and SELLER - each backed by its
 * own UserDetailsService/AuthenticationProvider so their credential stores
 * never mix. Authentication itself happens once, at /auth/**, where a JWT
 * carrying the role is issued. Every subsequent request is authorized by
 * JwtAuthFilter reading that token fresh - there is no server-side session
 * and no cookie fallback, so a request with a missing/invalid/expired
 * Authorization header is always rejected, regardless of what any earlier
 * request on the same client did.
 *
 * Note: we deliberately do NOT expose AuthenticationManager beans here.
 * Spring Security's own auto-config (HttpSecurityConfiguration) always
 * looks up a single shared AuthenticationManager bean internally, and two
 * of them with no @Primary breaks application startup. AuthController
 * builds its own ProviderManager instances directly from the
 * DaoAuthenticationProvider beans below, so that ambient lookup never sees
 * more than the one it expects (zero, in our case - which is fine, since
 * nothing here relies on it).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private SellerDetailsService sellerDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private SellerAccessInterceptor sellerAccessInterceptor;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Belt-and-braces check specifically for seller endpoints: confirms
        // there's a verified SELLER identity in the SecurityContext for
        // *this* request's token AND that it resolves to a real seller row,
        // before the controller (or its @RequestBody binding) ever runs.
        registry.addInterceptor(sellerAccessInterceptor).addPathPatterns(
                "/seller/apis/**", "/product/apis/addProduct", "/product/apis/*/images/**");
    }

    /** Used by AuthController to build its own ProviderManager for /auth/user/login. */
    @Bean
    public DaoAuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /** Used by AuthController to build its own ProviderManager for /auth/seller/login. */
    @Bean
    public DaoAuthenticationProvider sellerAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(sellerDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        // credential issuance is public by definition
                        .requestMatchers("/auth/**").permitAll()

                        // browsing the catalog doesn't require an account
                        .requestMatchers(HttpMethod.GET, "/product/apis/searchProduct/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product/apis/searchProduct").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product/apis/*").permitAll()

                        // uploaded product photos are public to view, same as the catalog itself
                        .requestMatchers(HttpMethod.GET, "/product-images/**").permitAll()

                        // only sellers manage catalog/inventory/orders/dashboards/photos
                        .requestMatchers("/product/apis/addProduct").hasRole("SELLER")
                        .requestMatchers("/product/apis/*/images/**").hasRole("SELLER")
                        .requestMatchers("/seller/apis/**").hasRole("SELLER")

                        // only buyers manage their profile
                        .requestMatchers("/u/apis/**").hasRole("USER")

                        // cart/checkout: buyers always; sellers too, since a seller's own
                        // login can also shop (see AuthenticatedAccountResolver#getCurrentShoppingUserId)
                        .requestMatchers("/cart/apis/**").hasAnyRole("USER", "SELLER")
                        .requestMatchers("/order/apis/**").hasAnyRole("USER", "SELLER")

                        // anything else still needs to be authenticated, whichever role
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
