package com.skillbridge.backend.contract;

import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.ACTIVE;

    private String submissionUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}