package com.whdgkr.tripsplite.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String loginId;
    private String password;
    private String name;
    private String email;
}
