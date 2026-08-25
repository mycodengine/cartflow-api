CREATE TABLE order_items (
    id          BIGSERIAL       PRIMARY KEY,
    order_id    BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT          NOT NULL REFERENCES products(id),
    product_name VARCHAR(200)   NOT NULL,
    unit_price  NUMERIC(12, 2)  NOT NULL,
    quantity    INTEGER         NOT NULL CHECK (quantity > 0),
    subtotal    NUMERIC(12, 2)  NOT NULL
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
