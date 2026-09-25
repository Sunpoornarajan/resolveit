package com.resolveit.service.impl;

import com.resolveit.dto.UserCreateDto;
import com.resolveit.dto.UserEditDto;
import com.resolveit.entity.Department;
import com.resolveit.entity.User;
import com.resolveit.enums.RoleType;
import com.resolveit.exception.DuplicateResourceException;
import com.resolveit.exception.ResourceNotFoundException;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveSupportEngineers() {
        return userRepository.findByRoleAndActiveTrueOrderByFirstNameAsc(RoleType.IT_SUPPORT);
    }

    @Override
    public User createUser(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username '" + dto.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' is already registered");
        }
        if (userRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID '" + dto.getEmployeeId() + "' already exists");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        User user = new User();
        user.setEmployeeId(dto.getEmployeeId().trim().toUpperCase());
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setUsername(dto.getUsername().trim().toLowerCase());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setDepartment(department);
        user.setActive(dto.isActive());

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, UserEditDto dto) {
        User user = findById(id);

        if (userRepository.existsByUsernameAndIdNot(dto.getUsername(), id)) {
            throw new DuplicateResourceException("Username '" + dto.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' is already registered");
        }
        if (userRepository.existsByEmployeeIdAndIdNot(dto.getEmployeeId(), id)) {
            throw new DuplicateResourceException("Employee ID '" + dto.getEmployeeId() + "' already exists");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        user.setEmployeeId(dto.getEmployeeId().trim().toUpperCase());
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setUsername(dto.getUsername().trim().toLowerCase());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setRole(dto.getRole());
        user.setDepartment(department);
        user.setActive(dto.isActive());

        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            if (dto.getNewPassword().trim().length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        }

        return userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = findById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(RoleType role) {
        return userRepository.countByRole(role);
    }
}
