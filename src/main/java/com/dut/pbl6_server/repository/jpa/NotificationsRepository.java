package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
}
