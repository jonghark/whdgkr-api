package com.whdgkr.tripsplite.dto;

import lombok.Data;

@Data
public class TripUpdateRequest {
    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
}
