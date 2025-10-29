package com.ijaes.jeogiyo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    private String password;
    private String name;
    private String address;
    private String phoneNumber;
    private boolean isOwner;
}
