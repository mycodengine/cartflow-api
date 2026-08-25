CREATE TABLE orders (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    subtotal        NUMERIC(12, 2)  NOT NULL,
    discount_amount NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    total           NUMERIC(12, 2)  NOT NULL,
    coupon_code     VARCHAR(50),
    shipping_address TEXT           NOT NULL,
    notes           VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
