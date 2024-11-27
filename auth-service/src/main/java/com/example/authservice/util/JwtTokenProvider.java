package com.example.authservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String redisEndpoint = "clustercfg.team2-redis.ykeerg.apn2.cache.amazonaws.com";
    private final int redisPort = 6379;
    private final JedisPool jedisPool = new JedisPool(redisEndpoint, redisPort);

    private final String secretKeyName = "JWT_TOKEN_KEY";
    private final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

    private SecretKey secretKey;

    public JwtTokenProvider() {
        this.secretKey = loadSecretKeyFromAws();
    }

    // Generate token
    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();

        // Store token in Redis
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(username, (int) (EXPIRATION_TIME / 1000), token);
        }
        return token;
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Load SECRET_KEY from AWS Secrets Manager
    private SecretKey loadSecretKeyFromAws() {
        Region region = Region.AP_NORTHEAST_2; // Change to your AWS region
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        GetSecretValueRequest secretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretKeyName)
                .build();

        GetSecretValueResponse secretValueResponse = secretsClient.getSecretValue(secretValueRequest);
        String secretKeyString = secretValueResponse.secretString();

        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // Retrieve token from Redis
    public String getTokenFromRedis(String username) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(username);
        }
    }

    // Remove token from Redis
    public void removeToken(String username) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(username);
        }
    }
}