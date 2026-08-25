package com.cartflow.controller;

import com.cartflow.domain.entity.User;
import com.cartflow.dto.request.CheckoutRequest;
import com.cartflow.dto.request.ReviewRequest;
import com.cartflow.dto.response.OrderResponse;
import com.cartflow.dto.response.PageResponse;
import com.cartflow.dto.response.ReviewResponse;
import com.cartflow.service.OrderService;
import com.cartflow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Orders & Reviews")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;

    @PostMapping("/orders/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout — places an order from the current cart")
    public OrderResponse checkout(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(user, request);
    }

    @GetMapping("/orders")
    @Operation(summary = "List my orders (paginated)")
    public PageResponse<OrderResponse> myOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.findMyOrders(user, PageRequest.of(page, size));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get order details by ID")
    public OrderResponse findById(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orderService.findById(id, user);
    }

    @PostMapping("/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a review for a product (one per user)")
    public ReviewResponse createReview(@AuthenticationPrincipal User user,
                                       @PathVariable Long productId,
                                       @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(productId, request, user);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete your own review")
    public void deleteReview(@AuthenticationPrincipal User user, @PathVariable Long reviewId) {
        reviewService.delete(reviewId, user);
    }
}
