package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Boolean isOwner;
    private String deleteYn;
    private String createdAt;
}
