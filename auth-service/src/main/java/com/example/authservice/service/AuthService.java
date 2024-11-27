package com.example.authservice.service;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Logger 객체 생성

    // 사용자 인증
    public boolean authenticate(String username, String rawPassword) {

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());

        return passwordMatches;
    }

    // 사용자 추가
    public User addUser(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(rawPassword);
        User savedUser = userRepository.save(user);

        return savedUser;
    }

    // 사용자 삭제
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 사용자 목록 가져오기
    public Iterable<User> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        return users;
    }
}