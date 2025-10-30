package com.ijaes.jeogiyo.auth.validator;

import com.ijaes.jeogiyo.auth.dto.request.SignUpRequest;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SignUpValidator {

    public void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_USERNAME);
        }

        if (username.length() < 4 || username.length() > 10) {
            throw new CustomException(ErrorCode.INVALID_USERNAME);
        }

        if (!username.matches("^[a-z0-9]+$")) {
            throw new CustomException(ErrorCode.INVALID_USERNAME);
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (password.length() < 8 || password.length() > 15) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (!password.matches(".*[a-z].*")) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (!password.matches(".*[0-9].*")) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/`~|\\\\].*")) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NAME);
        }

        if (name.length() < 3 || name.length() > 10) {
            throw new CustomException(ErrorCode.INVALID_NAME);
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PHONE_NUMBER);
        }

        if (!phoneNumber.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
            throw new CustomException(ErrorCode.INVALID_PHONE_NUMBER);
        }
    }

    public void validateAddress(String address) {
        if (address == null || address.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_ADDRESS);
        }

        if (address.length() > 100) {
            throw new CustomException(ErrorCode.INVALID_ADDRESS);
        }
    }

    public void validateSignUpRequest(SignUpRequest request) {
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
        validateName(request.getName());
        validatePhoneNumber(request.getPhoneNumber());
    }
}
