package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.entity.User;
import com.example.authservice.service.AuthService;
import com.example.authservice.util.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (authService.authenticate(username, password)) {
            String token = jwtTokenProvider.generateToken(username); // JWT 생성
            return ResponseEntity.ok(token); // JWT 반환
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // 사용자 추가
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestParam String username, @RequestParam String password) {
        User user = authService.addUser(username, password);
        return ResponseEntity.ok(user);
    }

    // 사용자 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 모든 사용자 조회
    @GetMapping("/users")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Authentification Service is healthy");
    }
}
