package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.UserDTO;
import com.rishanth.flux360.dto.UserStatsDTO;
import com.rishanth.flux360.entity.Role;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.entity.UserStatus;
import com.rishanth.flux360.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminUserService(
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /* ── Get all users ──────────────────────────────────────────────── */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    /* ── Stats ──────────────────────────────────────────────────────── */
    public UserStatsDTO getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus(UserStatus.ACTIVE);
        long blocked = userRepository.countByStatus(UserStatus.BLOCKED);
        long admins = userRepository.countByRole(Role.ROLE_ADMIN);

        return new UserStatsDTO(total, active, blocked, admins);
    }

    /* ── Block user ─────────────────────────────────────────────────── */
    @Transactional
    public UserDTO blockUser(Long userId) {

        User user = findUserById(userId);

        user.setStatus(UserStatus.BLOCKED);

        User savedUser = userRepository.save(user);

        auditLogService.log(
                "BLOCK_USER",
                getCurrentAdmin(),
                "Blocked user: " + user.getEmail()
        );

        return new UserDTO(savedUser);
    }

    /* ── Unblock user ───────────────────────────────────────────────── */
    @Transactional
    public UserDTO unblockUser(Long userId) {

        User user = findUserById(userId);

        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        auditLogService.log(
                "UNBLOCK_USER",
                getCurrentAdmin(),
                "Unblocked user: " + user.getEmail()
        );

        return new UserDTO(savedUser);
    }

    /* ── Delete user ────────────────────────────────────────────────── */
    @Transactional
    public void deleteUser(Long userId) {

        User user = findUserById(userId);

        auditLogService.log(
                "DELETE_USER",
                getCurrentAdmin(),
                "Deleted user: " + user.getEmail()
        );

        entityManager.createQuery(
                        "DELETE FROM Income i WHERE i.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        userRepository.delete(user);
    }

    /* ── Current Admin ──────────────────────────────────────────────── */
    private String getCurrentAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                ? authentication.getName()
                : "SYSTEM";
    }

    /* ── Private helper ─────────────────────────────────────────────── */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found: " + userId));
    }
}