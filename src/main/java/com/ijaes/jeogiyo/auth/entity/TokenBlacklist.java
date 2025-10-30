package com.ijaes.jeogiyo.auth.entity;

import com.ijaes.jeogiyo.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "j_token_blacklist", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_expiration_at", columnList = "expiration_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TokenBlacklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long expirationAt;
}
