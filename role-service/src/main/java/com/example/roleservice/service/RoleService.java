package com.example.roleservice.service;

import com.example.roleservice.entity.Role;
import com.example.roleservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    // 사용자 권한 조회
    public String getRoleByUsername(String username) {
        Optional<Role> roleOpt = roleRepository.findByUsername(username);
        return roleOpt.map(Role::getRole).orElse("UNKNOWN");
    }

    // 사용자 권한 설정
    public Role setRole(String username, String role) {
        Role roleEntity = roleRepository.findByUsername(username)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setUsername(username);
                    return newRole;
                });
        roleEntity.setRole(role);
        return roleRepository.save(roleEntity);
    }

    // 사용자 권한 삭제
    public void deleteRole(String username) {
        roleRepository.findByUsername(username).ifPresent(roleRepository::delete);
    }
}
