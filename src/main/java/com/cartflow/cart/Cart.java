package com.cartflow.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Full cart stored as a single value in Redis under key cart:{userId}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private Long userId;
    private List<CartItem> items = new ArrayList<>();

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
