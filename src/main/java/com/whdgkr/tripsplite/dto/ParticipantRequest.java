package com.whdgkr.tripsplite.dto;

import lombok.Data;

@Data
public class ParticipantRequest {
    private Long userId;
    private String name;
    private String phone;
    private String email;
}
