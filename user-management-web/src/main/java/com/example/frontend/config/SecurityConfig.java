package com.example.frontend.config;

import com.example.frontend.service.ApiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    private final ApiService apiService;

    // ApiService를 DI하여 받아옴
    public SecurityConfig(ApiService apiService) {
        this.apiService = apiService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/", "/home","/userManagement","/healthcheck").permitAll() // 로그인과 에러 페이지는 모두 접근 가능
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // 로그아웃 URL
                .logoutSuccessUrl("/login") // 로그아웃 후 로그인 페이지로 리다이렉트
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화
            .formLogin().disable(); // 기본 폼 로그인 비활성화

        return http.build();
    }

}