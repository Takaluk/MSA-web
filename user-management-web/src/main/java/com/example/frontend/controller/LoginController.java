package com.example.frontend.controller;

import com.example.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private ApiService apiService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long TOKEN_EXPIRATION_SECONDS = 3600; // 토큰 만료 시간: 1시간

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            String token = apiService.authenticateAndGetToken(username, password); // JWT 요청
            if (token != null) {
                // Redis에 JWT 저장 (key: username, value: token)
                redisTemplate.opsForValue().set(username, token, TOKEN_EXPIRATION_SECONDS, TimeUnit.SECONDS);

                // JWT에서 사용자 이름과 권한 추출
                String extractedUsername = apiService.getUsernameFromToken(token);
                String roles = apiService.getRole(username);

                // SecurityContextHolder에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        extractedUsername, null, AuthorityUtils.commaSeparatedStringToAuthorityList(roles)
                    )
                );
                return "redirect:/";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Server error. Please try again.");
            return "login";
        }
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Usermanagement Web server is healthy");
    }

    // Redis에서 토큰 가져오기 예제
    public String getTokenFromRedis(String username) {
        return redisTemplate.opsForValue().get(username);
    }
}
