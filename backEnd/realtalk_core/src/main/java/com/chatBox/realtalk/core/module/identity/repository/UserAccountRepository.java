package com.chatBox.realtalk.core.module.identity.repository;

import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
