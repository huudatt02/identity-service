package com.example.identity_service.user.specification;

import com.example.identity_service.user.entity.Role;
import com.example.identity_service.user.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern));
        };
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isBlank()) {
                return null;
            }

            Join<User, Role> role = root.join("roles", JoinType.INNER);

            return cb.equal(role.get("name"), roleName);
        };
    }

    public static Specification<User> hasEnabled(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) {
                return null;
            }

            return cb.equal(root.get("enabled"), enabled);
        };
    }
}
