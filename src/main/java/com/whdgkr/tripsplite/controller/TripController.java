package com.whdgkr.tripsplite.controller;

import com.whdgkr.tripsplite.dto.*;
import com.whdgkr.tripsplite.service.ExpenseService;
import com.whdgkr.tripsplite.service.SettlementService;
import com.whdgkr.tripsplite.service.StatisticsService;
import com.whdgkr.tripsplite.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;
    private final StatisticsService statisticsService;

    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/matched")
    public List<TripResponse> getMatchedTrips(@AuthenticationPrincipal Long memberId) {
        return tripService.getMatchedTrips(memberId);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTripById(@PathVariable Long tripId) {
        return tripService.getTripById(tripId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@AuthenticationPrincipal Long memberId, @RequestBody TripRequest request) {
        return tripService.createTrip(memberId, request);
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
    }

    @PatchMapping("/{tripId}")
    public TripResponse updateTrip(@PathVariable Long tripId, @RequestBody TripUpdateRequest request) {
        return tripService.updateTrip(tripId, request);
    }

    // Participant management endpoints
    @PostMapping("/{tripId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse addParticipant(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long tripId,
            @RequestBody ParticipantRequest request) {
        return tripService.addParticipant(memberId, tripId, request);
    }

    @DeleteMapping("/{tripId}/participants/{participantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParticipant(@PathVariable Long tripId, @PathVariable Long participantId) {
        tripService.deleteParticipant(tripId, participantId);
    }

    // Settlement endpoint
    @GetMapping("/{tripId}/settlement")
    public SettlementResponse getSettlement(
            @PathVariable Long tripId,
            @RequestParam(defaultValue = "UNSETTLED") String scope) {
        return settlementService.calculateSettlement(tripId, scope);
    }

    // Statistics endpoint
    @GetMapping("/{tripId}/statistics")
    public StatisticsResponse getStatistics(@PathVariable Long tripId) {
        return statisticsService.getTripStatistics(tripId);
    }

    // Expense endpoints
    @PostMapping("/{tripId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@PathVariable Long tripId, @RequestBody ExpenseRequest request) {
        return expenseService.createExpense(tripId, request);
    }

    @PutMapping("/expenses/{expenseId}")
    public ExpenseResponse updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(expenseId, request);
    }

    @DeleteMapping("/expenses/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
    }

    @PatchMapping("/expenses/{expenseId}/settled")
    public ExpenseResponse updateExpenseSettled(
            @PathVariable Long expenseId,
            @RequestBody ExpenseSettledRequest request) {
        return expenseService.updateSettledStatus(expenseId, Boolean.TRUE.equals(request.getSettled()));
    }
}
