package com.example.identity_service.user.repository;

import com.example.identity_service.user.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByNameIn(Collection<String> names);

    boolean existsByName(String name);
}
