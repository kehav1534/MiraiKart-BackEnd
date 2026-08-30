package com.eshu.OnlineShopping.controllers;

import com.eshu.OnlineShopping.dto.AuthResponseDto;
import com.eshu.OnlineShopping.dto.LoginRequestDto;
import com.eshu.OnlineShopping.dto.SellerRegisterDto;
import com.eshu.OnlineShopping.dto.UserRegisterDto;
import com.eshu.OnlineShopping.security.JwtUtil;
import com.eshu.OnlineShopping.service.SellerService;
import com.eshu.OnlineShopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * Owns signup and login for both account types. Registration creates the
 * credential (Auth) row and the profile (User/Seller) row together, so a
 * successful login always resolves to a complete, usable account.
 *
 * Login is verified with a ProviderManager built locally from the
 * user/seller DaoAuthenticationProvider beans, rather than a Spring-managed
 * AuthenticationManager bean - see the note in SecurityConfig for why.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private JwtUtil jwtUtil;

    private final AuthenticationManager userAuthenticationManager;
    private final AuthenticationManager sellerAuthenticationManager;

    @Autowired
    public AuthController(@Qualifier("userAuthenticationProvider") AuthenticationProvider userAuthenticationProvider,
                           @Qualifier("sellerAuthenticationProvider") AuthenticationProvider sellerAuthenticationProvider) {
        this.userAuthenticationManager = new ProviderManager(userAuthenticationProvider);
        this.sellerAuthenticationManager = new ProviderManager(sellerAuthenticationProvider);
    }

    // ---------- USER ----------

    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegisterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    @PostMapping("/user/login")
    public ResponseEntity<AuthResponseDto> loginUser(@RequestBody LoginRequestDto dto) {
        userAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        String token = jwtUtil.generateToken(dto.getEmail(), "USER");
        return ResponseEntity.ok(new AuthResponseDto(token, "USER", dto.getEmail()));
    }

    // ---------- SELLER ----------

    @PostMapping("/seller/register")
    public ResponseEntity<String> registerSeller(@RequestBody SellerRegisterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.registerSeller(dto));
    }

    @PostMapping("/seller/login")
    public ResponseEntity<AuthResponseDto> loginSeller(@RequestBody LoginRequestDto dto) {
        sellerAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        String token = jwtUtil.generateToken(dto.getEmail(), "SELLER");
        return ResponseEntity.ok(new AuthResponseDto(token, "SELLER", dto.getEmail()));
    }
}
