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

                // 동행자를 친구 목록에도 자동 등록
                autoRegisterAsFriend(info.getName(), info.getPhone(), info.getEmail());
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

        // 지출 날짜가 새 기간에 포함되는지 검증
        List<LocalDate> expenseDates = trip.getExpenses().stream()
                .filter(e -> "N".equals(e.getDeleteYn()))
                .map(e -> e.getOccurredAt().toLocalDate())
                .toList();

        for (LocalDate expenseDate : expenseDates) {
            if (expenseDate.isBefore(startDate) || expenseDate.isAfter(endDate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "지출 내역이 존재하는 날짜를 제외할 수 없습니다.");
            }
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

        // 동행자 추가 시 친구 목록에도 자동 등록 (phone 또는 email 있는 경우만)
        autoRegisterAsFriend(request.getName(), request.getPhone(), request.getEmail());

        return toParticipantResponse(saved);
    }

    /**
     * 동행자를 친구 목록에 자동 등록 (중복 방지)
     * - phone 또는 email 중 하나라도 있어야 등록
     * - 동일 phone 또는 email이 이미 존재하면 등록하지 않음
     */
    private void autoRegisterAsFriend(String name, String phone, String email) {
        // phone과 email 모두 없으면 친구 등록 생략
        if ((phone == null || phone.trim().isEmpty()) && (email == null || email.trim().isEmpty())) {
            return;
        }

        // 정규화
        String normalizedPhone = phone != null ? phone.replaceAll("[^0-9]", "") : null;
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;

        // 중복 체크: phone으로 검색
        if (normalizedPhone != null && !normalizedPhone.isEmpty()) {
            if (friendRepository.findByPhone(normalizedPhone).isPresent()) {
                return; // 이미 존재하면 등록하지 않음
            }
        }

        // 중복 체크: email로 검색
        if (normalizedEmail != null && !normalizedEmail.isEmpty()) {
            if (friendRepository.findByEmail(normalizedEmail).isPresent()) {
                return; // 이미 존재하면 등록하지 않음
            }
        }

        // 새 친구 등록
        Friend newFriend = Friend.builder()
                .name(name)
                .phone(normalizedPhone != null && !normalizedPhone.isEmpty() ? normalizedPhone : null)
                .email(normalizedEmail != null && !normalizedEmail.isEmpty() ? normalizedEmail : null)
                .build();

        friendRepository.save(newFriend);
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
                .settledYn(expense.getSettledYn())
                .settledAt(expense.getSettledAt() != null ? expense.getSettledAt().format(dateTimeFormatter) : null)
                .payments(payments)
                .shares(shares)
                .build();
    }
}
