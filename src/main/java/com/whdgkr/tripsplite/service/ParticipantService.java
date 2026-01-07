package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.ParticipantRequest;
import com.whdgkr.tripsplite.dto.ParticipantResponse;
import com.whdgkr.tripsplite.entity.Participant;
import com.whdgkr.tripsplite.entity.Trip;
import com.whdgkr.tripsplite.repository.ParticipantRepository;
import com.whdgkr.tripsplite.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {
    
    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipantsByTripId(Long tripId) {
        return participantRepository.findByTripId(tripId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ParticipantResponse addParticipant(Long tripId, ParticipantRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
        
        Participant participant = Participant.builder()
                .trip(trip)
                .name(request.getName())
                .build();
        
        Participant saved = participantRepository.save(participant);
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteParticipant(Long participantId) {
        participantRepository.deleteById(participantId);
    }
    
    private ParticipantResponse toResponse(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .name(participant.getName())
                .createdAt(participant.getCreatedAt().format(dateTimeFormatter))
                .build();
    }
}
