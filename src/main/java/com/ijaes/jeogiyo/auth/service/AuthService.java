package com.ijaes.jeogiyo.auth.service;

import com.ijaes.jeogiyo.auth.dto.AuthResponse;
import com.ijaes.jeogiyo.auth.dto.LoginRequest;
import com.ijaes.jeogiyo.auth.dto.SignUpRequest;
import com.ijaes.jeogiyo.auth.security.JwtUtil;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signUp(SignUpRequest request) {
        // 중복 아이디 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .message("회원가입이 성공했습니다.")
                .success(true)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // 사용자 확인
        var user = userRepository.findByUsername(request.getUsername());

        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        User foundUser = user.get();

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(foundUser.getUsername());

        return AuthResponse.builder()
                .message("로그인이 성공했습니다.")
                .token(token)
                .success(true)
                .build();
    }
}
