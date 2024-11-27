package com.example.frontend.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.frontend.dto.AddUserRequest;
import com.example.frontend.dto.LoginRequest;
import com.example.frontend.dto.SetRoleRequest;
import com.example.frontend.dto.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
@Service
public class ApiService {

    private final WebClient webClient;
    private static final String SECRET_KEY = "your-secret-key-that-is-at-least-256-bits-long";
    @Value("${auth-service.base-url}")
    private String authServiceUrl;

    @Value("${role-service.base-url}")
    private String roleServiceUrl;

    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
 // 사용자 인증 및 JWT 요청
    public String authenticateAndGetToken(String username, String password) {
        try {
            return webClient.post()
                    .uri(authServiceUrl + "/api/auth/authenticate")
                    .bodyValue(new LoginRequest(username, password))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // JWT를 반환받음
        } catch (Exception e) {
            return null;
        }
    }
 // 공통으로 JWT를 Authorization 헤더에 추가
    private WebClient getAuthorizedClient(String token) {
        return WebClient.builder()
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject(); // JWT의 subject가 사용자 이름입니다.
    }
 // Fetch all users from the auth-service
    public Iterable<User> getAllUsers() {
        try {
            return webClient.get()
                    .uri(authServiceUrl + "/api/auth/users")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Iterable<User>>() {})
                    .block();
        } catch (Exception e) {
            return null;
        }
    }


    // 사용자 추가 (auth-service)
    public boolean addUser(String username, String password) {
        try {
            webClient.post()
                .uri(authServiceUrl + "/api/auth/add")
                .bodyValue(new AddUserRequest(username, password))
                .retrieve()
                .toBodilessEntity()
                .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 사용자 삭제 (auth-service)
    public boolean deleteUser(Long userId) {
        try {
            webClient.delete()
                .uri(authServiceUrl + "/api/auth/delete/" + userId)
                .retrieve()
                .toBodilessEntity()
                .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // 사용자 권한 조회 (role-service)
    public String getRole(String username) {
        return webClient.get()
                .uri(roleServiceUrl + "/api/roles/" + username)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 사용자 권한 설정 (role-service)
    public boolean setRole(String username, String role) {
        try {
            webClient.post()
                .uri(roleServiceUrl + "/api/roles/set")
                .bodyValue(new SetRoleRequest(username, role))
                .retrieve()
                .toBodilessEntity()
                .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
