package com.cartflow.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** A single line item stored inside the Redis cart hash. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
