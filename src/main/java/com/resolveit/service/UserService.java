package com.resolveit.service;

import com.resolveit.dto.UserCreateDto;
import com.resolveit.dto.UserEditDto;
import com.resolveit.entity.User;
import com.resolveit.enums.RoleType;

import java.util.List;

public interface UserService {

    User findById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findAllUsers();

    List<User> findActiveSupportEngineers();

    User createUser(UserCreateDto dto);

    User updateUser(Long id, UserEditDto dto);

    void toggleUserStatus(Long id);

    long countTotalUsers();

    long countByRole(RoleType role);
}
