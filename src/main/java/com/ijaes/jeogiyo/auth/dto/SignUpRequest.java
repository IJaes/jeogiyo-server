package com.ijaes.jeogiyo.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isOwner")
    private boolean isOwner;
}
