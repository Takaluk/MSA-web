package com.example.frontend.controller;

import com.example.frontend.service.ApiService;
import com.example.frontend.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserManagementController {

    @Autowired
    private ApiService apiService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/userManagement")
    public String userManagement(Model model) {
        String username = "";
        String token = null;

        // 현재 사용자 식별 (예: SecurityContext에서 username 추출)
        try {
            username = apiService.getCurrentUsername(); // 현재 로그인된 사용자 이름
        } catch (Exception e) {
            model.addAttribute("error", "Failed to identify user.");
            return "userManagement";
        }

        if (!username.isEmpty()) {
            // Redis에서 JWT 가져오기
            token = redisTemplate.opsForValue().get(username);

            if (token == null) { // 인증되지 않은 사용자는 로그인 페이지로 리다이렉트
                return "redirect:/login";
            }

            // JWT에서 사용자 이름과 권한 확인
            String roles = apiService.getRole(username);

            // ADMIN 권한 검증
            if (!roles.contains("ADMIN")) {
                model.addAttribute("error", "Access Denied: Admins Only");
                return "error";
            }

            // 사용자 목록 가져오기
            Iterable<User> users = apiService.getAllUsers();

            // 각 사용자에 대해 역할을 가져와서 User 객체에 설정
            for (User user : users) {
                String role = apiService.getRole(user.getUsername()); // 각 사용자의 역할을 가져옵니다.
                user.setRole(role); // 가져온 역할을 User 객체에 설정
            }

            // 사용자 목록을 모델에 추가
            model.addAttribute("users", users);

            // 사용자 정보 모델에 추가
            model.addAttribute("username", username);
            return "userManagement"; // userManagement 페이지로 이동
        } else {
            model.addAttribute("error", "User is not logged in.");
            return "login"; // 로그인 페이지로 리다이렉트
        }
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam String username, @RequestParam String password) {
        boolean success = apiService.addUser(username, password);
        if (success) {
            return "redirect:/userManagement";
        } else {
            return "error";
        }
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Long userId) {
        boolean success = apiService.deleteUser(userId);
        if (success) {
            return "redirect:/userManagement";
        } else {
            return "error";
        }
    }
}
