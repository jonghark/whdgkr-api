package com.whdgkr.tripsplite.controller;

import com.whdgkr.tripsplite.dto.*;
import com.whdgkr.tripsplite.service.ExpenseService;
import com.whdgkr.tripsplite.service.SettlementService;
import com.whdgkr.tripsplite.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;

    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/{tripId}")
    public TripResponse getTripById(@PathVariable Long tripId) {
        return tripService.getTripById(tripId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
    }

    // Participant management endpoints
    @PostMapping("/{tripId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse addParticipant(@PathVariable Long tripId, @RequestBody ParticipantRequest request) {
        return tripService.addParticipant(tripId, request);
    }

    @DeleteMapping("/{tripId}/participants/{participantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParticipant(@PathVariable Long tripId, @PathVariable Long participantId) {
        tripService.deleteParticipant(tripId, participantId);
    }

    // Settlement endpoint
    @GetMapping("/{tripId}/settlement")
    public SettlementResponse getSettlement(@PathVariable Long tripId) {
        return settlementService.calculateSettlement(tripId);
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
}
