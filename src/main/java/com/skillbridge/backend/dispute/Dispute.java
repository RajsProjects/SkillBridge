package com.skillbridge.backend.dispute;

import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.common.enums.DisputeStatus;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.student.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opened_by_id", nullable = false)
    private User openedBy;

    @Column(nullable = false)
    private String openedByRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> proofUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    private LocalDateTime resolvedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}