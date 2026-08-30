package com.eshu.OnlineShopping.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Second, explicit gate in front of every seller endpoint.
 *
 * SecurityConfig's hasRole("SELLER") already blocks the request at the
 * filter-chain level if the SecurityContext - populated fresh by
 * JwtAuthFilter from this request's own Bearer token, since auth is
 * stateless - doesn't carry that authority. This interceptor re-checks
 * that on the way into the handler, and - unlike the filter-chain rule -
 * also resolves the identity all the way down to a real row in the seller
 * table via AuthenticatedAccountResolver. That catches cases the role
 * check alone can't: a token for an account that's since been removed, or
 * a SecurityContext some other code path set without going through
 * JwtAuthFilter. If either check fails, the seller controller method
 * never runs.
 */
@Component
public class SellerAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthenticatedAccountResolver authenticatedAccountResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isVerifiedSeller = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_SELLER"));

        if (!isVerifiedSeller) {
            throw new AccessDeniedException("This endpoint requires an authenticated seller session.");
        }

        // Resolves email -> real Seller row; throws NotFoundException (via
        // GlobalExceptionHandler -> 404) if the token/session no longer
        // maps to an actual seller account.
        authenticatedAccountResolver.getCurrentSellerId();

        return true;
    }
}
