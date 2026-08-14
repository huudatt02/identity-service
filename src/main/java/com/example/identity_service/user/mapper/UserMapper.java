package com.example.identity_service.user.mapper;

import com.example.identity_service.admin.dto.request.AdminUserCreateRequest;
import com.example.identity_service.auth.dto.request.RegisterRequest;
import com.example.identity_service.user.dto.request.UserCreateRequest;
import com.example.identity_service.user.dto.request.UserUpdateRequest;
import com.example.identity_service.user.dto.response.UserResponse;
import com.example.identity_service.user.entity.Role;
import com.example.identity_service.user.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toUser(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);

    UserCreateRequest toUserCreationRequest(RegisterRequest request);

    UserCreateRequest toUserCreationRequest(AdminUserCreateRequest request);

    @Mapping(target = "roles", source = "roles")
    UserResponse toUserResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
