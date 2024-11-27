package com.example.frontend.controller;

import com.example.frontend.service.ApiService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import com.example.frontend.dto.Partner;
@Controller
public class PartnerManagementController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/partnerManagement")
    public String partnerManagement(HttpSession session, Model model) {
        // 세션에서 JWT 가져오기
        String token = (String) session.getAttribute("token");
        if (token == null) { // 인증되지 않은 사용자는 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // JWT에서 사용자 이름과 권한 확인
        String username = apiService.getUsernameFromToken(token);
        String roles = apiService.getRole(username);

        // ADMIN 권한 검증
        if (!roles.contains("ADMIN")) {
            model.addAttribute("error", "Access Denied: Admins Only");
            return "error";
        }

        // 사용자 목록 가져오기
        Iterable<Partner> partners = apiService.getAllPartners();

        // 사용자 목록을 모델에 추가
        model.addAttribute("users", partners);

        return "partnerManagement"; // partnerManagement 페이지로 이동
    }
}
