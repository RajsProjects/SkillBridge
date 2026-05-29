package com.skillbridge.backend.user.student;


import com.skillbridge.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String college;
    private String city;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT[]")
    @Convert(converter = StringListConverter.class)
    private List<String> skills;

    @Column(columnDefinition = "TEXT[]")
    @Convert(converter = StringListConverter.class)
    private List<String> portfolioLinks;

    private String githubUrl;
    private String linkedinUrl;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    private String profileImage;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}