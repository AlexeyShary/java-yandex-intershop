CREATE TABLE product
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255),
    description TEXT,
    img_path    VARCHAR(255),
    price       DECIMAL(10, 2)
);

CREATE TABLE orders
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    total_sum DECIMAL(10, 2)
);

CREATE TABLE order_item
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT,
    product_id  BIGINT,
    title       VARCHAR(255),
    description TEXT,
    img_path    VARCHAR(255),
    count       INT,
    price       DECIMAL(10, 2),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

INSERT INTO product (title, description, img_path, price)
VALUES ('Товар 1', 'Описание товара 1', '/img/img-1.jpg', '100.00'),
       ('Товар 2', 'Описание товара 2', '/img/img-2.jpg', '250.00'),
       ('Товар 3', 'Описание товара 3', '/img/img-3.jpg', '1000.00');