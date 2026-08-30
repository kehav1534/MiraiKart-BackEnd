package com.eshu.OnlineShopping.service;

import com.eshu.OnlineShopping.model.SellerAuth;
import com.eshu.OnlineShopping.model.SellerPrincipal;
import com.eshu.OnlineShopping.repository.SellerAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads seller credentials for authentication. Used only by the seller
 * DaoAuthenticationProvider - kept separate from user auth so a seller
 * account and a user account can never be confused for one another.
 */
@Service
public class SellerDetailsService implements UserDetailsService {

    @Autowired
    SellerAuthRepository sellerAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SellerAuth seller = sellerAuthRepository.findByEmail(username);
        if (seller == null) {
            throw new UsernameNotFoundException("Seller not found");
        }
        return new SellerPrincipal(seller);
    }
}
