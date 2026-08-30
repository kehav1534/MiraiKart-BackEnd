package com.eshu.OnlineShopping.service;

import com.eshu.OnlineShopping.model.UserAuth;
import com.eshu.OnlineShopping.model.UserPrincipal;
import com.eshu.OnlineShopping.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user (buyer) credentials for authentication. Used only by the user
 * DaoAuthenticationProvider - kept separate from seller auth so a user
 * account and a seller account can never be confused for one another.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuth userAuth = userAuthRepository.findByEmail(username);
        if (userAuth == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new UserPrincipal(userAuth);
    }
}
