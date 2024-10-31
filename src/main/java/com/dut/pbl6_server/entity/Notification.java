package com.dut.pbl6_server.entity;

import com.dut.pbl6_server.common.model.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "notifications")
public class Notification extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Account receiver;

    private Long objectId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String content; // In Vietnamese by default

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean publicAdminFlag = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean publicUserFlag = false;
}
