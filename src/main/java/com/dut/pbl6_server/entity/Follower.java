package com.dut.pbl6_server.entity;

import com.dut.pbl6_server.common.model.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "followers")
public class Follower extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private Account follower; // 'follower' is the user who follows the user

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Account user; // 'user' is the user who is followed by the follower
}
