package com.cartflow.mapper;

import com.cartflow.domain.entity.Order;
import com.cartflow.domain.entity.OrderItem;
import com.cartflow.dto.response.OrderItemResponse;
import com.cartflow.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toItemResponse(OrderItem item);
}
