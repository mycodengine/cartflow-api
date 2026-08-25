CREATE TABLE coupons (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(255),
    discount_type   VARCHAR(20)     NOT NULL, -- PERCENTAGE | FIXED_AMOUNT
    discount_value  NUMERIC(10, 2)  NOT NULL CHECK (discount_value > 0),
    min_order_value NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    max_uses        INTEGER,
    uses_count      INTEGER         NOT NULL DEFAULT 0,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coupons_code ON coupons(code);
