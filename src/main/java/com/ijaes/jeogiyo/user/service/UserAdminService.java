package com.ijaes.jeogiyo.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.dto.UpdateRoleRequest;
import com.ijaes.jeogiyo.user.dto.UserInfoResponse;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
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
				.id(user.getId())
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
			.id(user.getId())
			.username(user.getUsername())
			.name(user.getName())
			.address(user.getAddress())
			.phoneNumber(user.getPhoneNumber())
			.role(user.getRole().getAuthority())
			.build();
	}

	public UserInfoResponse updateUserRole(UUID userId, UpdateRoleRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		try {
			Role newRole = Role.valueOf(request.getRole());
			user.setRole(newRole);
			userRepository.save(user);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_ROLE);
		}

		return UserInfoResponse.builder()
			.id(user.getId())
			.username(user.getUsername())
			.name(user.getName())
			.address(user.getAddress())
			.phoneNumber(user.getPhoneNumber())
			.role(user.getRole().getAuthority())
			.build();
	}
}
