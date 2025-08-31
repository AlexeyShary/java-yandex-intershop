package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.entity.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}