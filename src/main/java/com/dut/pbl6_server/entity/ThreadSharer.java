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
@Table(name = "thread_sharers")
public class ThreadSharer extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;
}
