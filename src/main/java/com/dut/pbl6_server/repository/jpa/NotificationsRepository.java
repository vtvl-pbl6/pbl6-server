package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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

    @Query("""
            SELECT CASE
                WHEN COUNT(n) > 0 THEN TRUE
                ELSE FALSE
            END
            FROM Notification n
            WHERE
                n.sender.id = :senderId
                AND n.objectId = :threadId
                AND n.type = 'REQUEST_THREAD_MODERATION'
                AND n.deletedAt IS NULL
        """)
    boolean isAlreadyRequestModeration(Long senderId, Long threadId);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE
                n.type = 'REQUEST_THREAD_MODERATION'
                AND n.deletedAt IS NULL
                AND (
                    SELECT COUNT(n2)
                    FROM Notification n2
                    WHERE
                        n2.objectId = n.objectId
                        AND (n2.type = 'REQUEST_THREAD_MODERATION_SUCCESS' OR n2.type = 'REQUEST_THREAD_MODERATION_FAILED')
                        AND n2.deletedAt IS NULL
                ) = 0
        """)
    List<Notification> getRequestModerateThreadIds();

    @Query("""
            SELECT n
            FROM Notification n
            WHERE
                n.type = 'REQUEST_THREAD_MODERATION'
                AND n.objectId = :threadId
                AND n.deletedAt IS NULL
        """)
    Notification getRequestModerateThreadById(Long threadId);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE
                (n.type = 'REQUEST_THREAD_MODERATION_SUCCESS' OR n.type = 'REQUEST_THREAD_MODERATION_FAILED')
                AND n.objectId = :threadId
                AND n.deletedAt IS NULL
            ORDER BY n.createdAt DESC
            LIMIT 1
        """)
    Notification getResponseModerateThreadById(Long threadId);
}