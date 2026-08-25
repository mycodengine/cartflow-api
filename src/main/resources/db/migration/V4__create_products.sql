CREATE TABLE products (
    id           BIGSERIAL       PRIMARY KEY,
    category_id  BIGINT          NOT NULL REFERENCES categories(id),
    name         VARCHAR(200)    NOT NULL,
    description  TEXT,
    price        NUMERIC(12, 2)  NOT NULL CHECK (price >= 0),
    stock        INTEGER         NOT NULL DEFAULT 0 CHECK (stock >= 0),
    sku          VARCHAR(100)    NOT NULL UNIQUE,
    image_url    VARCHAR(500),
    active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active    ON products(active);
CREATE INDEX idx_products_sku       ON products(sku);
