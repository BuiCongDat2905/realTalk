package com.chatBox.realtalk.core.module.identity.repository;

import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<UserAccount, Long> {

}
