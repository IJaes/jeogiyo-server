package com.ijaes.jeogiyo.auth.service;

import com.ijaes.jeogiyo.auth.dto.response.AuthResponse;
import com.ijaes.jeogiyo.auth.dto.request.LoginRequest;
import com.ijaes.jeogiyo.auth.dto.request.SignUpRequest;
import com.ijaes.jeogiyo.auth.entity.TokenBlacklist;
import com.ijaes.jeogiyo.auth.repository.TokenBlacklistRepository;
import com.ijaes.jeogiyo.auth.security.JwtUtil;
import com.ijaes.jeogiyo.auth.validator.SignUpValidator;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SignUpValidator signUpValidator;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public AuthResponse signUp(SignUpRequest request) {
        signUpValidator.validateSignUpRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .isOwner(request.isOwner())
                .role(request.isOwner() ? Role.OWNER : Role.USER)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .message("회원가입이 성공했습니다.!")
                .success(true)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername());

        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        User foundUser = user.get();

        if (foundUser.getRole().equals(Role.BLOCK)) {
            throw new CustomException(ErrorCode.BLOCKED_USER);
        }

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        String token = jwtUtil.generateToken(foundUser.getUsername());

        return AuthResponse.builder()
                .message("로그인이 성공했습니다.")
                .token(token)
                .role(foundUser.getRole().getAuthority())
                .success(true)
                .build();
    }

    public AuthResponse logout(String bearerToken) {
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        String username = jwtUtil.extractUsername(token);

        jwtUtil.validateToken(token);

        long expirationAt = jwtUtil.getTokenExpirationTime(token);

        String tokenHash = hashToken(token);

        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .tokenHash(tokenHash)
                .username(username)
                .expirationAt(expirationAt)
                .build();

        tokenBlacklistRepository.save(tokenBlacklist);

        return AuthResponse.builder()
                .message("로그아웃이 성공했습니다.")
                .success(true)
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
