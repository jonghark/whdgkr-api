package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.FriendRequest;
import com.whdgkr.tripsplite.dto.FriendResponse;
import com.whdgkr.tripsplite.entity.Friend;
import com.whdgkr.tripsplite.entity.Member;
import com.whdgkr.tripsplite.repository.FriendRepository;
import com.whdgkr.tripsplite.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<FriendResponse> getAllFriends() {
        return friendRepository.findAllByOrderByNameAsc().stream()
                .filter(Friend::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getMyFriends(Long memberId) {
        return friendRepository.findByOwnerMemberIdAndDeleteYnOrderByNameAsc(memberId, "N").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FriendResponse getFriendById(Long id) {
        Friend friend = friendRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found: " + id));
        if (!friend.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found: " + id);
        }
        return toResponse(friend);
    }

    @Transactional
    public FriendResponse createFriend(Long ownerMemberId, FriendRequest request) {
        Member ownerMember = memberRepository.findById(ownerMemberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        Friend friend = Friend.builder()
                .ownerMember(ownerMember)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        // loginId 또는 email로 회원 검색하여 매칭
        if (StringUtils.hasText(request.getFriendLoginId())) {
            memberRepository.findByLoginIdAndDeleteYn(request.getFriendLoginId(), "N")
                    .ifPresent(friend::matchWithMember);
        } else if (StringUtils.hasText(request.getEmail())) {
            memberRepository.findByEmailAndDeleteYn(request.getEmail(), "N")
                    .ifPresent(friend::matchWithMember);
        }

        friend = friendRepository.save(friend);
        return toResponse(friend);
    }

    @Transactional
    public FriendResponse updateFriend(Long id, FriendRequest request) {
        Friend friend = friendRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found: " + id));

        if (!friend.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found: " + id);
        }

        friend.setName(request.getName());
        friend.setEmail(request.getEmail());
        friend.setPhone(request.getPhone());

        // 매칭 재시도
        if (!friend.isMatched()) {
            if (StringUtils.hasText(request.getFriendLoginId())) {
                memberRepository.findByLoginIdAndDeleteYn(request.getFriendLoginId(), "N")
                        .ifPresent(friend::matchWithMember);
            } else if (StringUtils.hasText(request.getEmail())) {
                memberRepository.findByEmailAndDeleteYn(request.getEmail(), "N")
                        .ifPresent(friend::matchWithMember);
            }
        }

        friend = friendRepository.save(friend);
        return toResponse(friend);
    }

    @Transactional
    public void deleteFriend(Long id) {
        Friend friend = friendRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found: " + id));
        friend.setDeleteYn("Y");
        friendRepository.save(friend);
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
                .friendMemberId(friend.getFriendMember() != null ? friend.getFriendMember().getId() : null)
                .matchedYn(friend.getMatchedYn())
                .matchedAt(friend.getMatchedAt())
                .createdAt(friend.getCreatedAt())
                .build();
    }
}
