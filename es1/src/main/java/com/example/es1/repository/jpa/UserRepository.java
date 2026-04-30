package com.example.es1.repository.jpa;


import com.example.es1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(String role);

    List<User> findByStatus(Integer status);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Integer userId, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    void updateStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    Page<User> findByUsernameContainingOrRealNameContaining(String username, String realName, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByRoleAndStatus(String role, Integer status, Pageable pageable);
}
