package com.example.onlinebankingsystem.repository;

import com.example.onlinebankingsystem.models.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}