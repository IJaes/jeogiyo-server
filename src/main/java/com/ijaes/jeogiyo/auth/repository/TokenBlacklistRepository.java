package com.ijaes.jeogiyo.auth.repository;

import com.ijaes.jeogiyo.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {
    Optional<TokenBlacklist> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    @Query("DELETE FROM TokenBlacklist t WHERE t.expirationAt < :currentTimeMillis")
    void deleteExpiredTokens(long currentTimeMillis);
}
