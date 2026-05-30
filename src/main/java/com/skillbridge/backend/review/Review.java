package com.skillbridge.backend.review;

import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id", "reviewer_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private User reviewedUser;

    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}