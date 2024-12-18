package com.example.frontend.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.frontend.dto.LoginRequest;
import com.example.frontend.dto.Partner;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class ApiService {

    private final WebClient webClient;
    private final StringRedisTemplate redisTemplate;
    private String secretKey;

    @Value("${auth-service.base-url}")
    private String authServiceUrl;

    @Value("${role-service.base-url}")
    private String roleServiceUrl;

    @Value("${carbon-emmision-service.base-url}")
    private String partnerServiceUrl;

    @Value("${redis.jwt.prefix:jwt:}")
    private String redisJwtPrefix;

    @Autowired
    public ApiService(WebClient.Builder webClientBuilder, StringRedisTemplate redisTemplate, SecretsManagerClient secretsManagerClient) {
        this.webClient = webClientBuilder.build();
        this.redisTemplate = redisTemplate;
        this.secretKey = fetchSecretKey(secretsManagerClient);
    }

    private String fetchSecretKey(SecretsManagerClient secretsManagerClient) {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId("JWT_TOKEN_KEY")
                .build();
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            return response.secretString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch secret key from AWS Secrets Manager", e);
        }
    }

    // 사용자 인증 및 JWT 요청
    public String authenticateAndGetToken(String username, String password) {
        try {
            String token = webClient.post()
                    .uri(authServiceUrl + "/api/auth/authenticate")
                    .bodyValue(new LoginRequest(username, password))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Redis에 JWT 저장
            if (token != null) {
                redisTemplate.opsForValue().set(redisJwtPrefix + username, token);
            }
            return token;
        } catch (Exception e) {
            return null;
        }
    }

    // Redis에서 JWT 읽기
    public String getTokenFromRedis(String username) {
        return redisTemplate.opsForValue().get(redisJwtPrefix + username);
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
    public Iterable<Partner> getAllPartners() {
        try {
            return webClient.get()
                    .uri(partnerServiceUrl + "/api/partners/carbon")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Iterable<Partner>>() {})
                    .block();
        } catch (Exception e) {
            return null;
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
}
