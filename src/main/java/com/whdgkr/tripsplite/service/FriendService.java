package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.FriendRequest;
import com.whdgkr.tripsplite.dto.FriendResponse;
import com.whdgkr.tripsplite.entity.Friend;
import com.whdgkr.tripsplite.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;

    @Transactional(readOnly = true)
    public List<FriendResponse> getAllFriends() {
        return friendRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FriendResponse getFriendById(Long id) {
        Friend friend = friendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Friend not found: " + id));
        return toResponse(friend);
    }

    @Transactional
    public FriendResponse createFriend(FriendRequest request) {
        Friend friend = Friend.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        friend = friendRepository.save(friend);
        return toResponse(friend);
    }

    @Transactional
    public FriendResponse updateFriend(Long id, FriendRequest request) {
        Friend friend = friendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Friend not found: " + id));

        friend.setName(request.getName());
        friend.setEmail(request.getEmail());
        friend.setPhone(request.getPhone());

        friend = friendRepository.save(friend);
        return toResponse(friend);
    }

    @Transactional
    public void deleteFriend(Long id) {
        if (!friendRepository.existsById(id)) {
            throw new RuntimeException("Friend not found: " + id);
        }
        friendRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Friend> getFriendsByIds(List<Long> ids) {
        return friendRepository.findByIdIn(ids);
    }

    private FriendResponse toResponse(Friend friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .name(friend.getName())
                .email(friend.getEmail())
                .phone(friend.getPhone())
                .createdAt(friend.getCreatedAt())
                .build();
    }
}
