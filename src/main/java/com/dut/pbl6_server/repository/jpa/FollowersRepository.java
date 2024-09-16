package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowersRepository extends JpaRepository<Follower, Long> {
}
