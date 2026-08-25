package com.cartflow.service;

import com.cartflow.domain.entity.Product;
import com.cartflow.domain.entity.Review;
import com.cartflow.domain.entity.User;
import com.cartflow.dto.request.ReviewRequest;
import com.cartflow.dto.response.PageResponse;
import com.cartflow.dto.response.ReviewResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.ReviewMapper;
import com.cartflow.repository.ProductRepository;
import com.cartflow.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    public PageResponse<ReviewResponse> findByProduct(Long productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findAllByProductId(productId, pageable);
        return new PageResponse<>(page.getContent().stream().map(reviewMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional
    public ReviewResponse create(Long productId, ReviewRequest request, User user) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new BusinessException("You have already reviewed this product");
        }

        Review review = reviewRepository.save(Review.builder()
                .product(product)
                .user(user)
                .rating(request.rating())
                .title(request.title())
                .body(request.body())
                .build());
        log.info("Review created for product {} by user {}", productId, user.getEmail());
        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void delete(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Review not found: " + reviewId);
        }
        reviewRepository.delete(review);
        log.info("Review {} deleted by user {}", reviewId, user.getEmail());
    }
}
