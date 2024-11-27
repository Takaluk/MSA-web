package com.example.frontend.controller;

import com.example.frontend.service.ApiService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String root() {
        return "redirect:/home"; // 루트 경로 접속 시 /home으로 리다이렉트
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token"); // 세션에서 JWT 가져오기

        if (token != null) {
            // JWT에서 사용자 이름과 권한 추출
            String username = apiService.getUsernameFromToken(token);
            String roles = apiService.getRole(username);

            model.addAttribute("username", username);
            model.addAttribute("role", roles);
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
