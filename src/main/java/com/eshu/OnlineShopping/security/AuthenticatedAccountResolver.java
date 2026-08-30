package com.eshu.OnlineShopping.security;

import com.eshu.OnlineShopping.exceptions.NotFoundException;
import com.eshu.OnlineShopping.model.Seller;
import com.eshu.OnlineShopping.model.User;
import com.eshu.OnlineShopping.repository.SellerRepository;
import com.eshu.OnlineShopping.repository.UserInfoRepository;
import com.eshu.OnlineShopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The one place that turns "who does this request's JWT say it is" into a
 * real, database-backed identity.
 *
 * JwtAuthFilter already verified the token's signature and expiry and put a
 * role-scoped Authentication (principal = the token's email claim) into the
 * SecurityContext before this ever runs, and SecurityConfig's
 * authorizeHttpRequests rules already rejected the request if that role
 * isn't allowed on the endpoint. What's left is identity: a seller's or
 * buyer's numeric id must never be taken from the request body/params, or
 * one authenticated account could act as another simply by naming its id.
 * Every service call that needs "the current seller" or "the current user"
 * gets it from here instead, which resolves strictly off the verified
 * email in the SecurityContext.
 */
@Component
public class AuthenticatedAccountResolver {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserService userService;

    /** The verified email (JWT subject) of whoever is making this request. */
    public String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated session found. Please log in again.");
        }
        return authentication.getName();
    }

    /** The current authenticated role, e.g. "USER" or "SELLER" (without the ROLE_ prefix). */
    public String getCurrentRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .orElseThrow(() -> new AccessDeniedException("No authenticated session found. Please log in again."));
    }

    /** Resolves the buyer id that belongs to the authenticated USER token. */
    public int getCurrentUserId() {
        String email = getCurrentEmail();
        User user = userInfoRepository.findByUserAuth_Email(email)
                .orElseThrow(() -> new NotFoundException("User account", email));
        return user.getId();
    }

    /** Resolves the seller id that belongs to the authenticated SELLER token. */
    public int getCurrentSellerId() {
        String email = getCurrentEmail();
        Seller seller = sellerRepository.findBySellerAuth_Email(email)
                .orElseThrow(() -> new NotFoundException("Seller account", email));
        return seller.getId();
    }

    /**
     * Resolves "which cart/order profile" for whoever is making this
     * request, whether they logged in as a buyer or as a seller. A USER
     * token resolves exactly as getCurrentUserId() does. A SELLER token
     * resolves to that seller's auto-provisioned shopping profile (created
     * on first use - see UserService#getOrCreateShoppingProfileForSeller),
     * so a seller can browse, add to cart, and check out using the same
     * login they manage their store with, without a separate buyer signup.
     */
    public int getCurrentShoppingUserId() {
        String role = getCurrentRole();
        if ("USER".equals(role)) {
            return getCurrentUserId();
        }
        if ("SELLER".equals(role)) {
            return userService.getOrCreateShoppingProfileForSeller(getCurrentSellerId()).getId();
        }
        throw new AccessDeniedException("This account type cannot shop.");
    }
}
