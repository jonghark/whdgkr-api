package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TripResponse {
    private Long id;
    private String name;
    private String startDate;
    private String endDate;
    private String deleteYn;
    private String createdAt;
    private ParticipantResponse owner;
    private List<ParticipantResponse> participants;
    private List<ExpenseResponse> expenses;
}
