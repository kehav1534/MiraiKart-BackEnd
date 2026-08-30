package com.eshu.OnlineShopping.dto;

public class AuthResponseDto {
    private final String token;
    private final String role;
    private final String email;

    public AuthResponseDto(String token, String role, String email) {
        this.token = token;
        this.role = role;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
