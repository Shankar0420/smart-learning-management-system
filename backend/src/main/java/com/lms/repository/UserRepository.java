package com.lms.repository;

import com.lms.entity.ApprovalStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    Optional<User> findByProfessionalId(String professionalId);

    boolean existsByProfessionalId(String professionalId);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    List<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);

    long countByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);

    long countByActive(boolean active);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "(u.professionalId IS NOT NULL AND LOWER(u.professionalId) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
           "(u.employeeId IS NOT NULL AND LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
           "(u.enrollmentNumber IS NOT NULL AND LOWER(u.enrollmentNumber) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<User> searchUsers(@Param("q") String query);
}
