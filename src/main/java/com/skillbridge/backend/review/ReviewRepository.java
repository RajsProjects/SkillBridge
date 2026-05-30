package com.skillbridge.backend.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByReviewedUserId(UUID userId, Pageable pageable);

    boolean existsByContractIdAndReviewerId(UUID contractId, UUID reviewerId);

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM Review r
        WHERE r.reviewedUser.id = :userId
    """)
    Double getAverageRatingByUserId(@Param("userId") UUID userId);

    long countByReviewedUserId(UUID userId);
}