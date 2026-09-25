package com.resolveit.repository;

import com.resolveit.entity.User;
import com.resolveit.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);

    List<User> findByRoleAndActiveTrueOrderByFirstNameAsc(RoleType role);

    long countByRole(RoleType role);

    long countByRoleAndActiveTrue(RoleType role);

    List<User> findAllByOrderByCreatedAtDesc();
}
