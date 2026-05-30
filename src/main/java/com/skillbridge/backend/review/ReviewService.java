package com.skillbridge.backend.review;

import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.contract.ContractRepository;
import com.skillbridge.backend.review.dto.RatingResponse;
import com.skillbridge.backend.review.dto.ReviewRequest;
import com.skillbridge.backend.review.dto.ReviewResponse;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse submit(UUID reviewerId, ReviewRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Can only review completed contracts");
        }

        boolean isClient  = contract.getClient().getId().equals(reviewerId);
        boolean isStudent = contract.getStudent().getId().equals(reviewerId);

        if (!isClient && !isStudent) {
            throw new UnauthorizedException("You are not part of this contract");
        }

        if (reviewRepository.existsByContractIdAndReviewerId(
                contract.getId(), reviewerId)) {
            throw new BadRequestException("You have already reviewed this contract");
        }

        // Client reviews student, student reviews client
        User reviewedUser = isClient
                ? contract.getStudent()
                : contract.getClient();

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = Review.builder()
                .contract(contract)
                .reviewer(reviewer)
                .reviewedUser(reviewedUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByReviewedUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RatingResponse getRating(UUID userId) {
        return RatingResponse.builder()
                .averageRating(reviewRepository.getAverageRatingByUserId(userId))
                .totalReviews(reviewRepository.countByReviewedUserId(userId))
                .build();
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .contractId(review.getContract().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getName())
                .reviewedUserId(review.getReviewedUser().getId())
                .reviewedUserName(review.getReviewedUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}