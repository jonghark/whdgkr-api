package com.whdgkr.tripsplite.controller;

import com.whdgkr.tripsplite.dto.FriendRequest;
import com.whdgkr.tripsplite.dto.FriendResponse;
import com.whdgkr.tripsplite.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public List<FriendResponse> getMyFriends(@AuthenticationPrincipal Long memberId) {
        return friendService.getMyFriends(memberId);
    }

    @GetMapping("/{id}")
    public FriendResponse getFriendById(@PathVariable Long id) {
        return friendService.getFriendById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendResponse createFriend(
            @AuthenticationPrincipal Long memberId,
            @RequestBody FriendRequest request) {
        return friendService.createFriend(memberId, request);
    }

    @PutMapping("/{id}")
    public FriendResponse updateFriend(@PathVariable Long id, @RequestBody FriendRequest request) {
        return friendService.updateFriend(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
    }
}
