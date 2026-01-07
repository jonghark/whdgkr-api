package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByOrderByNameAsc();
    List<Friend> findByIdIn(List<Long> ids);
}
