package com.ijaes.jeogiyo.user.service;

import com.ijaes.jeogiyo.auth.validator.SignUpValidator;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.dto.request.UpdateAddressRequest;
import com.ijaes.jeogiyo.user.dto.request.UpdatePasswordRequest;
import com.ijaes.jeogiyo.user.dto.request.UpdatePhoneNumberRequest;
import com.ijaes.jeogiyo.user.dto.response.SimpleUpdateResponse;
import com.ijaes.jeogiyo.user.dto.response.UserInfoResponse;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignUpValidator signUpValidator;

    public SimpleUpdateResponse updateAddress(Authentication authentication, UpdateAddressRequest request) {
        User user = getAuthenticatedUser(authentication);
        user.updateAddress(request.getAddress());
        userRepository.save(user);
        return SimpleUpdateResponse.builder()
            .message("주소가 수정되었습니다.")
            .success(true)
            .build();
    }

    public SimpleUpdateResponse updatePhoneNumber(Authentication authentication, UpdatePhoneNumberRequest request) {
        User user = getAuthenticatedUser(authentication);
        user.updatePhoneNumber(request.getPhoneNumber());
        userRepository.save(user);
        return SimpleUpdateResponse.builder()
            .message("전화번호가 수정되었습니다.")
            .success(true)
            .build();
    }

    public SimpleUpdateResponse updatePassword(Authentication authentication, UpdatePasswordRequest request) {
        User user = getAuthenticatedUser(authentication);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new CustomException(ErrorCode.DUPLICATE_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return SimpleUpdateResponse.builder()
            .message("비밀번호가 수정되었습니다.")
            .success(true)
            .build();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return UserInfoResponse.builder()
            .name(user.getName())
            .username(user.getUsername())
            .id(user.getId())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .role(String.valueOf(user.getRole()))
            .build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}

