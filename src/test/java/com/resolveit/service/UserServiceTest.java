package com.resolveit.service;

import com.resolveit.dto.UserCreateDto;
import com.resolveit.entity.Department;
import com.resolveit.entity.User;
import com.resolveit.enums.RoleType;
import com.resolveit.exception.DuplicateResourceException;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.repository.UserRepository;
import com.resolveit.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        department.setId(1L);
    }

    @Test
    @DisplayName("Should create user with BCrypt encoded password and active flag set")
    void testCreateUser_Success() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmployeeId("EMP-2001");
        dto.setFirstName("Alice");
        dto.setLastName("Wonder");
        dto.setUsername("alicew");
        dto.setEmail("alice@test.com");
        dto.setPassword("Secret@123");
        dto.setRole(RoleType.IT_SUPPORT);
        dto.setDepartmentId(1L);
        dto.setActive(true);

        when(userRepository.existsByUsername("alicew")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.existsByEmployeeId("EMP-2001")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(dto);

        assertNotNull(created);
        assertEquals("EMP-2001", created.getEmployeeId());
        assertEquals("alicew", created.getUsername());
        assertEquals("$2a$10$encodedPasswordHash", created.getPassword());
        assertEquals(RoleType.IT_SUPPORT, created.getRole());
        assertTrue(created.isActive());

        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating user with existing username")
    void testCreateUser_DuplicateUsername_ThrowsException() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating user with existing email")
    void testCreateUser_DuplicateEmail_ThrowsException() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("newuser");
        dto.setEmail("existing@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }
}
