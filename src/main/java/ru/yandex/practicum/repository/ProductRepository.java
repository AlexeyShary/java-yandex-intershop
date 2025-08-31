package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.model.entity.Product;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

}