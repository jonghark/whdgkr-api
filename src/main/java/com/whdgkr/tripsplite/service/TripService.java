package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.*;
import com.whdgkr.tripsplite.entity.Friend;
import com.whdgkr.tripsplite.entity.Participant;
import com.whdgkr.tripsplite.entity.Trip;
import com.whdgkr.tripsplite.repository.FriendRepository;
import com.whdgkr.tripsplite.repository.ParticipantRepository;
import com.whdgkr.tripsplite.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final FriendRepository friendRepository;
    private final ParticipantRepository participantRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {
        return tripRepository.findByDeleteYn("N").stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
        return toDetailResponse(trip);
    }

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        // Validate: at least one participant required
        if ((request.getParticipants() == null || request.getParticipants().isEmpty()) &&
            (request.getFriendIds() == null || request.getFriendIds().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one participant is required");
        }

        // Validate: exactly one owner required
        long ownerCount = 0;
        if (request.getParticipants() != null) {
            ownerCount = request.getParticipants().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsOwner()))
                    .count();
        }
        if (ownerCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exactly one owner must be specified");
        }

        Trip trip = Trip.builder()
                .title(request.getTripName())
                .startDate(LocalDate.parse(request.getStartDate(), dateFormatter))
                .endDate(LocalDate.parse(request.getEndDate(), dateFormatter))
                .build();

        Trip saved = tripRepository.save(trip);
        List<Participant> participants = new ArrayList<>();

        // Create participants from friendIds if provided
        if (request.getFriendIds() != null && !request.getFriendIds().isEmpty()) {
            List<Friend> friends = friendRepository.findByIdIn(request.getFriendIds());
            for (Friend friend : friends) {
                Participant participant = Participant.builder()
                        .trip(saved)
                        .name(friend.getName())
                        .phone(friend.getPhone())
                        .build();
                participants.add(participantRepository.save(participant));
            }
        }

        // Create participants from detailed participant info
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            for (TripRequest.ParticipantInfo info : request.getParticipants()) {
                Participant participant = Participant.builder()
                        .trip(saved)
                        .name(info.getName())
                        .phone(info.getPhone())
                        .email(info.getEmail())
                        .isOwner(Boolean.TRUE.equals(info.getIsOwner()))
                        .build();
                participants.add(participantRepository.save(participant));
            }
        }

        saved.setParticipants(participants);
        return toDetailResponse(saved);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
        trip.setDeleteYn("Y");
        tripRepository.save(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        LocalDate startDate = LocalDate.parse(request.getStartDate(), dateFormatter);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), dateFormatter);

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
        }

        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        Trip saved = tripRepository.save(trip);

        return toDetailResponse(saved);
    }

    @Transactional
    public ParticipantResponse addParticipant(Long tripId, ParticipantRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        Participant participant = Participant.builder()
                .trip(trip)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .isOwner(false)
                .build();

        Participant saved = participantRepository.save(participant);
        return toParticipantResponse(saved);
    }

    @Transactional
    public void deleteParticipant(Long tripId, Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found: " + participantId));

        if (!participant.getTrip().getId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant does not belong to this trip");
        }

        if (Boolean.TRUE.equals(participant.getIsOwner())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the owner of the trip");
        }

        participant.setDeleteYn("Y");
        participantRepository.save(participant);
    }

    private ParticipantResponse toParticipantResponse(Participant p) {
        return ParticipantResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .isOwner(p.getIsOwner())
                .deleteYn(p.getDeleteYn())
                .createdAt(p.getCreatedAt().format(dateTimeFormatter))
                .build();
    }

    private TripResponse toSimpleResponse(Trip trip) {
        List<ParticipantResponse> participants = trip.getParticipants().stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());

        Participant owner = trip.getOwner();

        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getTitle())
                .startDate(trip.getStartDate().format(dateFormatter))
                .endDate(trip.getEndDate().format(dateFormatter))
                .deleteYn(trip.getDeleteYn())
                .createdAt(trip.getCreatedAt().format(dateTimeFormatter))
                .owner(owner != null ? toParticipantResponse(owner) : null)
                .participants(participants)
                .build();
    }

    private TripResponse toDetailResponse(Trip trip) {
        List<ParticipantResponse> participants = trip.getParticipants().stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());

        List<ExpenseResponse> expenses = trip.getExpenses().stream()
                .filter(e -> "N".equals(e.getDeleteYn()))
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());

        Participant owner = trip.getOwner();

        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getTitle())
                .startDate(trip.getStartDate().format(dateFormatter))
                .endDate(trip.getEndDate().format(dateFormatter))
                .deleteYn(trip.getDeleteYn())
                .createdAt(trip.getCreatedAt().format(dateTimeFormatter))
                .owner(owner != null ? toParticipantResponse(owner) : null)
                .participants(participants)
                .expenses(expenses)
                .build();
    }

    private ExpenseResponse toExpenseResponse(com.whdgkr.tripsplite.entity.Expense expense) {
        List<ExpenseResponse.PaymentDetail> payments = expense.getPayments().stream()
                .filter(p -> "N".equals(p.getDeleteYn()))
                .map(p -> ExpenseResponse.PaymentDetail.builder()
                        .participantId(p.getParticipant().getId())
                        .participantName(p.getParticipant().getName())
                        .amount(p.getAmount())
                        .build())
                .collect(Collectors.toList());

        List<ExpenseResponse.ShareDetail> shares = expense.getShares().stream()
                .filter(s -> "N".equals(s.getDeleteYn()))
                .map(s -> ExpenseResponse.ShareDetail.builder()
                        .participantId(s.getParticipant().getId())
                        .participantName(s.getParticipant().getName())
                        .amount(s.getAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .occurredAt(expense.getOccurredAt().format(dateTimeFormatter))
                .totalAmount(expense.getTotalAmount())
                .currency(expense.getCurrency())
                .createdAt(expense.getCreatedAt().format(dateTimeFormatter))
                .payments(payments)
                .shares(shares)
                .build();
    }
}
