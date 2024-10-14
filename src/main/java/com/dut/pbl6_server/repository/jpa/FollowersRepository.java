package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Follower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowersRepository extends JpaRepository<Follower, Long> {
    List<Follower> findAllByFollowerId(Long followerId);

    Page<Follower> findAllByUserId(Long userId, Pageable pageable);

    // Check if the follower is following the user
    @Query(
        "SELECT " +
            "CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Follower f " +
            "WHERE f.user.id = :userId AND f.follower.id = :followerId"
    )
    boolean isFollowing(Long userId, Long followerId);
}
