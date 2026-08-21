package com.chatBox.realtalk.core.module.identity.repository;

import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount,Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<UserAccount> findByUsername(String username);
    UserAccount findByEmail(String email);
    Optional<UserAccount> findByPublicId(UUID publicId);

    Optional<UserAccount> findBySystemRole(UserRole systemRole);
}
