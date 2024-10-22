package com.dut.pbl6_server.entity;

import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.entity.json.HosResult;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "threads")
public class Thread extends AbstractEntity implements Cloneable {
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Account author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_thread_id")
    private Thread parentThread;

    private String content;

    @Type(JsonBinaryType.class)
    private List<HosResult> hosResult;

    @Column(nullable = false)
    @Builder.Default
    private int reactionNum = 0;

    @Column(nullable = false)
    @Builder.Default
    private int sharedNum = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPin = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private ThreadStatus status = ThreadStatus.CREATING;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    //
    // Relationships
    //
    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY)
    private List<ThreadFile> files;

    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY)
    private List<ThreadSharer> sharers;

    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY)
    private List<ThreadReactUser> reactUsers;

    @OneToMany(mappedBy = "parentThread", fetch = FetchType.LAZY)
    private List<Thread> comments;

    @Override
    public Thread clone() {
        try {
            return (Thread) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
