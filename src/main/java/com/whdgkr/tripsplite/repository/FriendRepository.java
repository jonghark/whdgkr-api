package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByOrderByNameAsc();
    List<Friend> findByIdIn(List<Long> ids);
    List<Friend> findByOwnerMemberIdAndDeleteYnOrderByNameAsc(Long ownerMemberId, String deleteYn);

    Optional<Friend> findByPhone(String phone);
    Optional<Friend> findByEmail(String email);
    Optional<Friend> findByOwnerMemberIdAndEmailAndDeleteYn(Long ownerMemberId, String email, String deleteYn);
    Optional<Friend> findByOwnerMemberIdAndFriendIdAndDeleteYn(Long ownerMemberId, String friendId, String deleteYn);

    // friendId 목록 조회 (여행 매칭용)
    List<Friend> findByOwnerMemberIdAndDeleteYn(Long ownerMemberId, String deleteYn);
}
