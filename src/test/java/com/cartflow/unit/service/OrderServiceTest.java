package com.cartflow.unit.service;

import com.cartflow.domain.entity.Order;
import com.cartflow.domain.enums.OrderStatus;
import com.cartflow.exception.BusinessException;
import com.cartflow.event.OrderEventProducer;
import com.cartflow.mapper.OrderMapper;
import com.cartflow.repository.OrderRepository;
import com.cartflow.repository.ProductRepository;
import com.cartflow.service.CartService;
import com.cartflow.service.CouponService;
import com.cartflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock CartService cartService;
    @Mock CouponService couponService;
    @Mock OrderEventProducer eventProducer;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderService orderService;

    @Test
    void updateStatus_transitionsPENDING_toCONFIRMED() {
        Order order = order(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(null);

        orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(eventProducer).publishStatusChanged(any());
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        Order order = order(2L, OrderStatus.DELIVERED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(2L, OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_allowsCANCELLED_fromPENDING() {
        Order order = order(3L, OrderStatus.PENDING);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(null);

        orderService.updateStatus(3L, OrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void updateStatus_allowsSHIPPED_toDELIVERED() {
        Order order = order(4L, OrderStatus.SHIPPED);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(null);

        orderService.updateStatus(4L, OrderStatus.DELIVERED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    private Order order(Long id, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setStatus(status);
        return o;
    }
}
