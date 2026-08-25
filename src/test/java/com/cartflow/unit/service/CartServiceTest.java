package com.cartflow.unit.service;

import com.cartflow.cart.Cart;
import com.cartflow.cart.CartItem;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.CartItemRequest;
import com.cartflow.dto.response.CartResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.repository.ProductRepository;
import com.cartflow.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOps;
    @Mock ProductRepository productRepository;
    @InjectMocks CartService cartService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void addItem_addsNewItemToEmptyCart() {
        when(valueOps.get("cart:1")).thenReturn(null);
        Product product = product(1L, "Widget", new BigDecimal("25.00"), 10);
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        CartResponse response = cartService.addItem(1L, new CartItemRequest(1L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.total()).isEqualByComparingTo("50.00");
        verify(valueOps).set(eq("cart:1"), any(Cart.class), any());
    }

    @Test
    void addItem_incrementsExistingItem() {
        Cart existing = new Cart(1L, new ArrayList<>());
        existing.getItems().add(new CartItem(1L, "Widget", new BigDecimal("25.00"), 3));
        when(valueOps.get("cart:1")).thenReturn(existing);
        Product product = product(1L, "Widget", new BigDecimal("25.00"), 10);
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        CartResponse response = cartService.addItem(1L, new CartItemRequest(1L, 2));

        assertThat(response.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void addItem_throwsWhenInsufficientStock() {
        when(valueOps.get("cart:1")).thenReturn(null);
        Product product = product(1L, "Widget", new BigDecimal("25.00"), 1);
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, new CartItemRequest(1L, 5)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only 1 units available");
    }

    @Test
    void addItem_throwsWhenProductNotFound() {
        when(valueOps.get("cart:1")).thenReturn(null);
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(1L, new CartItemRequest(99L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_removesFromCart() {
        Cart existing = new Cart(1L, new ArrayList<>());
        existing.getItems().add(new CartItem(1L, "Widget", new BigDecimal("25.00"), 2));
        when(valueOps.get("cart:1")).thenReturn(existing);

        CartResponse response = cartService.removeItem(1L, 1L);

        assertThat(response.items()).isEmpty();
    }

    private Product product(Long id, String name, BigDecimal price, int stock) {
        return Product.builder().id(id).name(name).price(price).stock(stock).active(true).build();
    }
}
