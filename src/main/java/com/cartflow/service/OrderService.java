package com.cartflow.service;

import com.cartflow.cart.Cart;
import com.cartflow.cart.CartItem;
import com.cartflow.domain.entity.*;
import com.cartflow.domain.enums.OrderStatus;
import com.cartflow.dto.request.CheckoutRequest;
import com.cartflow.dto.response.OrderResponse;
import com.cartflow.dto.response.PageResponse;
import com.cartflow.event.OrderEventProducer;
import com.cartflow.event.OrderPlacedEvent;
import com.cartflow.event.OrderStatusChangedEvent;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.InsufficientStockException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.OrderMapper;
import com.cartflow.repository.OrderRepository;
import com.cartflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final OrderEventProducer eventProducer;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        Cart cart = cartService.getCartInternal(user.getId());
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout with an empty cart");
        }

        // Build items, decrement stock atomically
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            int updated = productRepository.decrementStock(product.getId(), cartItem.getQuantity());
            if (updated == 0) {
                throw new InsufficientStockException(product.getName(), cartItem.getQuantity(), product.getStock());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItems.add(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .build());
            subtotal = subtotal.add(itemSubtotal);
        }

        // Apply coupon discount if provided
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCouponCode = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            discount = couponService.apply(request.couponCode(), subtotal);
            appliedCouponCode = request.couponCode().toUpperCase();
        }

        Order order = Order.builder()
                .user(user)
                .subtotal(subtotal)
                .discountAmount(discount)
                .total(subtotal.subtract(discount))
                .couponCode(appliedCouponCode)
                .shippingAddress(request.shippingAddress())
                .notes(request.notes())
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);
        cartService.clearCart(user.getId());

        eventProducer.publishOrderPlaced(new OrderPlacedEvent(
                saved.getId(), user.getId(), saved.getTotal(), saved.getShippingAddress(), LocalDateTime.now()));

        log.info("Order {} placed by user {}", saved.getId(), user.getEmail());
        return orderMapper.toResponse(saved);
    }

    public PageResponse<OrderResponse> findMyOrders(User user, Pageable pageable) {
        var page = orderRepository.findAllByUserId(user.getId(), pageable);
        return new PageResponse<>(page.getContent().stream().map(orderMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public OrderResponse findById(Long id, User user) {
        Order order = getOrThrow(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }
        return orderMapper.toResponse(order);
    }

    /** Admin: transitions the order to the requested status following the state machine. */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = getOrThrow(id);
        OrderStatus previous = order.getStatus();
        validateTransition(previous, newStatus);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        eventProducer.publishStatusChanged(new OrderStatusChangedEvent(
                saved.getId(), saved.getUser().getId(), previous, newStatus, LocalDateTime.now()));
        log.info("Order {} status: {} → {}", id, previous, newStatus);
        return orderMapper.toResponse(saved);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        boolean valid = switch (from) {
            case PENDING    -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED  -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED;
            case SHIPPED    -> to == OrderStatus.DELIVERED || to == OrderStatus.REFUNDED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("Invalid status transition: " + from + " → " + to);
        }
    }

    private Order getOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }
}
