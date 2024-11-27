package com.example.frontend.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.frontend.dto.AddUserRequest;
import com.example.frontend.dto.LoginRequest;
import com.example.frontend.dto.SetRoleRequest;
import com.example.frontend.dto.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class ApiService {

    private final WebClient webClient;
    
    @Value("${auth-service.base-url}")
    private String authServiceUrl;

    @Value("${role-service.base-url}")
    private String roleServiceUrl;

    @Value("clustercfg.team2-redis.ykeerg.apn2.cache.amazonaws.com")
    private String redisHost;

    @Value("6379")
    private int redisPort;

    private String secretKey;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    private void initializeSecretKey() {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.create();
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId("JWT_TOKEN_KEY")
                .build();
        GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
        this.secretKey = getSecretValueResponse.secretString();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    // 사용자 인증 및 JWT 요청
    public String authenticateAndGetToken(String username, String password) {
        try {
            String token = webClient.post()
                    .uri(authServiceUrl + "/api/auth/authenticate")
                    .bodyValue(new LoginRequest(username, password))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // JWT를 반환받음

            if (token != null) {
                // Redis에 JWT 저장 (사용자 이름을 키로 사용)
                redisTemplate.opsForValue().set(username, token);
            }
            return token;
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
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
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

    // Redis에서 JWT 가져오기
    public String getTokenFromRedis(String username) {
        return redisTemplate.opsForValue().get(username); // Redis에서 사용자 이름을 키로 사용하여 JWT 가져오기
    }

    // Redis에서 JWT 삭제 (로그아웃 시 사용)
    public void deleteTokenFromRedis(String username) {
        redisTemplate.delete(username); // Redis에서 해당 사용자 키로 저장된 JWT 삭제
    }
}
