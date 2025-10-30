package com.ijaes.jeogiyo.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
