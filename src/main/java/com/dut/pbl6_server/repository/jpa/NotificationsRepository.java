package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    /* Receiver's role:
     *  user -> admin:null, admin:user, user:user
     *  admin -> user:null, user:admin
     */
    @Query("""
            SELECT n
            FROM Notification n
            LEFT JOIN FETCH n.sender ns
            LEFT JOIN FETCH ns.avatarFile
            LEFT JOIN FETCH n.receiver nr
            LEFT JOIN FETCH nr.avatarFile
            WHERE
                (
                    n.receiver.id = :receiverId
                    OR (n.receiver IS NULL AND :receiverRole = 'USER' AND n.publicUserFlag = TRUE)
                    OR (n.receiver IS NULL AND :receiverRole = 'ADMIN' AND n.publicAdminFlag = TRUE)
                )
                AND n.deletedAt IS NULL
        """)
    Page<Notification> getNotificationsByReceiverId(Long receiverId, String receiverRole, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.customContent IS NOT NULL AND n.deletedAt IS NULL")
    Page<Notification> getCreatedNotifications(Pageable pageable);
}