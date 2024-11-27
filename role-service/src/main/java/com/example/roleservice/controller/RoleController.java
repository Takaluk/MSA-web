package com.example.roleservice.controller;

import com.example.roleservice.entity.Role;
import com.example.roleservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // 사용자 권한 조회
    @GetMapping("/{username}")
    public ResponseEntity<String> getRole(@PathVariable String username) {
        String role = roleService.getRoleByUsername(username);
        return ResponseEntity.ok(role);
    }

    // 사용자 권한 설정
    @PostMapping("/set")
    public ResponseEntity<Role> setRole(@RequestParam String username, @RequestParam String role) {
        Role updatedRole = roleService.setRole(username, role);
        return ResponseEntity.ok(updatedRole);
    }

    // 사용자 권한 삭제
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteRole(@PathVariable String username) {
        roleService.deleteRole(username);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Role Service is healthy");
    }
}
