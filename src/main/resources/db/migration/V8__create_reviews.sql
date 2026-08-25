CREATE TABLE reviews (
    id         BIGSERIAL    PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    rating     SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title      VARCHAR(200),
    body       TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    -- One review per user per product
    CONSTRAINT uq_review_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_reviews_product ON reviews(product_id);
