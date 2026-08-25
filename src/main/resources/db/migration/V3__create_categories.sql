CREATE TABLE categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    slug        VARCHAR(120) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO categories (name, slug, description) VALUES
    ('Electronics',    'electronics',    'Gadgets, phones, and tech accessories'),
    ('Clothing',       'clothing',       'Men, women, and kids fashion'),
    ('Home & Kitchen', 'home-kitchen',   'Furniture, appliances, and decor'),
    ('Books',          'books',          'Fiction, non-fiction, and textbooks'),
    ('Sports',         'sports',         'Equipment and sportswear'),
    ('Beauty',         'beauty',         'Skincare, makeup, and fragrances');
