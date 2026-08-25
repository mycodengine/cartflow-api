package com.cartflow.service;

import com.cartflow.cart.Cart;
import com.cartflow.cart.CartItem;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.CartItemRequest;
import com.cartflow.dto.response.CartItemResponse;
import com.cartflow.dto.response.CartResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the user's cart using Redis.
 * Cart is stored as a single JSON blob under key cart:{userId}.
 * Every mutation resets the 7-day TTL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    @Value("${cart.ttl-seconds:604800}")
    private long cartTtlSeconds;

    public CartResponse getCart(Long userId) {
        Cart cart = loadCart(userId);
        return toResponse(cart);
    }

    /** Used internally by OrderService — returns the raw Cart to avoid double DTO conversion. */
    public Cart getCartInternal(Long userId) {
        return loadCart(userId);
    }

    public CartResponse addItem(Long userId, CartItemRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        if (product.getStock() < request.quantity()) {
            throw new BusinessException("Only " + product.getStock() + " units available for: " + product.getName());
        }

        Cart cart = loadCart(userId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        // Update quantity if item already in cart, otherwise add new
        boolean found = false;
        for (CartItem item : items) {
            if (item.getProductId().equals(request.productId())) {
                item.setQuantity(item.getQuantity() + request.quantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(new CartItem(product.getId(), product.getName(), product.getPrice(), request.quantity()));
        }

        cart.setItems(items);
        saveCart(userId, cart);
        log.debug("Added productId={} qty={} to cart for userId={}", request.productId(), request.quantity(), userId);
        return toResponse(cart);
    }

    public CartResponse updateItem(Long userId, Long productId, int quantity) {
        Cart cart = loadCart(userId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        if (quantity <= 0) {
            items.removeIf(i -> i.getProductId().equals(productId));
        } else {
            items.stream().filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(i -> i.setQuantity(quantity));
        }

        cart.setItems(items);
        saveCart(userId, cart);
        return toResponse(cart);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        return updateItem(userId, productId, 0);
    }

    public void clearCart(Long userId) {
        redisTemplate.delete(cartKey(userId));
        log.debug("Cart cleared for userId={}", userId);
    }

    // --- Helpers ---

    private Cart loadCart(Long userId) {
        Object raw = redisTemplate.opsForValue().get(cartKey(userId));
        if (raw instanceof Cart cart) {
            return cart;
        }
        return new Cart(userId, new ArrayList<>());
    }

    private void saveCart(Long userId, Cart cart) {
        redisTemplate.opsForValue().set(cartKey(userId), cart, Duration.ofSeconds(cartTtlSeconds));
    }

    private String cartKey(Long userId) {
        return "cart:" + userId;
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(i.getProductId(), i.getProductName(),
                        i.getUnitPrice(), i.getQuantity(), i.getSubtotal()))
                .toList();
        return new CartResponse(cart.getUserId(), items, cart.getTotal(), cart.getTotalItems());
    }
}
