package com.example.frontend.controller;

import com.example.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    private final JedisPool jedisPool = new JedisPool("clustercfg.team2-redis.ykeerg.apn2.cache.amazonaws.com", 6379);

    @GetMapping("/")
    public String root() {
        return "redirect:/home"; // 루트 경로 접속 시 /home으로 리다이렉트
    }

    @GetMapping("/home")
    public String home(Model model) {
        String token = null;

        // Redis에서 JWT 가져오기
        try (Jedis jedis = jedisPool.getResource()) {
            token = jedis.get("userToken"); // 키는 사용자별로 설정 가능
        } catch (Exception e) {
            model.addAttribute("error", "Unable to connect to Redis.");
            return "home";
        }

        if (token != null) {
            try {
                // JWT에서 사용자 이름과 권한 추출
                String username = apiService.getUsernameFromToken(token);
                String roles = apiService.getRole(username);

                model.addAttribute("username", username);
                model.addAttribute("role", roles);
            } catch (Exception e) {
                model.addAttribute("error", "Invalid token or user information.");
            }
        } else {
            model.addAttribute("error", "User is not logged in.");
        }

        return "home"; // home 페이지 렌더링
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Partner Web server is healthy");
    }
}
