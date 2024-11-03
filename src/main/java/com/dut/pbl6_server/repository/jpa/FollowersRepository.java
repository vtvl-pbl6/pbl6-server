package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Follower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowersRepository extends JpaRepository<Follower, Long> {
    @Query("SELECT f FROM Follower f WHERE  f.follower.id = :followerId AND f.deletedAt IS NULL")
    List<Follower> findAllByFollowerId(Long followerId);

    @Query("SELECT f.user.id FROM Follower f WHERE  f.follower.id = :followerId AND f.deletedAt IS NULL")
    Page<Long> findFollowingUserIds(Long followerId, Pageable pageable);

    @Query("SELECT f.follower.id FROM Follower f WHERE  f.user.id = :userId AND f.deletedAt IS NULL")
    Page<Long> findFollowerIds(Long userId, Pageable pageable);

    Optional<Follower> findByFollowerIdAndUserId(Long followerId, Long userId);

    // Check if the follower is following the user
    @Query(
        "SELECT " +
            "CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Follower f " +
            "WHERE f.user.id = :userId AND f.follower.id = :followerId AND f.deletedAt IS NULL"
    )
    boolean isFollowing(Long userId, Long followerId);
}