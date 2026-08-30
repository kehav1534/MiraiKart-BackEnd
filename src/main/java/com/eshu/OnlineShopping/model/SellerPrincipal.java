package com.eshu.OnlineShopping.model;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class SellerPrincipal implements UserDetails {

    private SellerAuth sellerAuth;

    public SellerPrincipal(SellerAuth seller) {
        this.sellerAuth = seller;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_SELLER"));
    }

    @Override
    public @Nullable String getPassword() {
        return sellerAuth.getPassword();
    }

    @Override
    public String getUsername() {
        return sellerAuth.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
