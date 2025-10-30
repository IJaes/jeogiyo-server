package com.ijaes.jeogiyo.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.dto.UserInfoResponse;
import com.ijaes.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdminService {

	private final UserRepository userRepository;

	public List<UserInfoResponse> getAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(user -> UserInfoResponse.builder()
						.username(user.getUsername())
						.name(user.getName())
						.address(user.getAddress())
						.phoneNumber(user.getPhoneNumber())
						.role(user.getRole().getAuthority())
						.build())
				.toList();
	}

	public UserInfoResponse getUserById(UUID userId) {
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		return UserInfoResponse.builder()
				.username(user.getUsername())
				.name(user.getName())
				.address(user.getAddress())
				.phoneNumber(user.getPhoneNumber())
				.role(user.getRole().getAuthority())
				.build();
	}
}
