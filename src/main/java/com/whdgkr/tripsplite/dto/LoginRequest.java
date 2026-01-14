package com.whdgkr.tripsplite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영문 소문자와 숫자만 가능합니다")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "^[0-9]{4}$", message = "비밀번호는 숫자 4자리만 가능합니다")
    private String password;
}
