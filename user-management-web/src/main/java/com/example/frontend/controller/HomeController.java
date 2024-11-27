package com.example.frontend.controller;

import com.example.frontend.service.ApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/")
    public String root() {
        return "redirect:/home"; // 루트 경로 접속 시 /home으로 리다이렉트
    }

    @GetMapping("/home")
    public String home(Model model) {
        String username = ""; // 현재 사용자 식별용 변수
        String token = null;

        // 여기에서 현재 사용자의 식별자를 가져오는 로직 필요 (예: SecurityContext에서 추출)
        try {
            username = apiService.getCurrentUsername(); // 예시 메서드
        } catch (Exception e) {
            model.addAttribute("error", "Failed to identify user.");
            return "home";
        }

        if (!username.isEmpty()) {
            // Redis에서 JWT 가져오기
            token = redisTemplate.opsForValue().get(username);

            if (token != null) {
                // JWT에서 사용자 이름과 권한 추출
                String extractedUsername = apiService.getUsernameFromToken(token);
                String roles = apiService.getRole(extractedUsername);

                model.addAttribute("username", extractedUsername);
                model.addAttribute("role", roles);
            } else {
                model.addAttribute("error", "Token not found or expired. Please log in again.");
            }
        } else {
            model.addAttribute("error", "User is not logged in.");
        }

        return "home"; // home 페이지 렌더링
    }

}
