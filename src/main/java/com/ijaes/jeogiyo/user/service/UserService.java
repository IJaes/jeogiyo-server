package com.ijaes.jeogiyo.user.service;

import com.ijaes.jeogiyo.auth.validator.SignUpValidator;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.dto.*;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignUpValidator signUpValidator;

    public UserUpdateResponse updateAddress(Authentication authentication, UpdateAddressRequest request) {
        User user = (User) authentication.getPrincipal();

        signUpValidator.validateAddress(request.getAddress());

        user.setAddress(request.getAddress());
        userRepository.save(user);

        return UserUpdateResponse.builder()
            .message("주소가 수정되었습니다.")
            .success(true)
            .username(user.getUsername())
            .name(user.getName())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    public UserUpdateResponse updatePhoneNumber(Authentication authentication, UpdatePhoneNumberRequest request) {
        User user = (User) authentication.getPrincipal();

        signUpValidator.validatePhoneNumber(request.getPhoneNumber());

        user.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(user);

        return UserUpdateResponse.builder()
            .message("전화번호가 수정되었습니다.")
            .success(true)
            .username(user.getUsername())
            .name(user.getName())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    public UserUpdateResponse updatePassword(Authentication authentication, UpdatePasswordRequest request) {
        User user = (User) authentication.getPrincipal();

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_CURRENT_PASSWORD);
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_ID_PW);
        }

        signUpValidator.validatePassword(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return UserUpdateResponse.builder()
            .message("비밀번호가 수정되었습니다.")
            .success(true)
            .username(user.getUsername())
            .name(user.getName())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

	public UserInfoResponse getUserInfo(Authentication authentication) {
        User user = (User)authentication.getPrincipal();
        return UserInfoResponse.builder()
            .name(user.getName())
            .username(user.getUsername())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .role(String.valueOf(user.getRole()))
            .build();
    }
}
