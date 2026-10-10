package com.lms.repository;

import com.lms.entity.ApprovalStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    List<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);

    long countByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);
}

