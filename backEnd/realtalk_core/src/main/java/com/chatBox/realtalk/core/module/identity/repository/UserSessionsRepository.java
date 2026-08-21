package com.chatBox.realtalk.core.module.identity.repository;

import com.chatBox.realtalk.core.module.identity.entity.UserSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSessionsRepository extends JpaRepository<UserSessions, Long> {
    Boolean existsByPublicId(UUID publicId);
    @Query("select count(s) > 0 from UserSessions s where s.tokenHash = :tokenHash")
    boolean existsByTokenHash(@Param("tokenHash") byte[] tokenHash);
}
