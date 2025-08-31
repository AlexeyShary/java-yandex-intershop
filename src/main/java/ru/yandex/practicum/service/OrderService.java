package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.model.entity.OrderItem;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    public Mono<Order> buyItems(String sessionId) {
        return cartService.getAll(sessionId).collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.empty();
                    }

                    BigDecimal total = cartItems.stream()
                            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getCount())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Order toSave = Order.builder()
                            .totalSum(total)
                            .build();

                    return orderRepository.save(toSave)
                            .flatMap(savedOrder -> {
                                List<OrderItem> orderItems = cartItems.stream()
                                        .map(item -> OrderItem.builder()
                                                .orderId(savedOrder.getId())
                                                .productId(item.getId())
                                                .title(item.getTitle())
                                                .description(item.getDescription())
                                                .imgPath(item.getImgPath())
                                                .price(item.getPrice())
                                                .count(item.getCount())
                                                .build())
                                        .collect(java.util.stream.Collectors.toList());

                                return orderItemRepository.saveAll(orderItems)
                                        .then(cartService.clearCart(sessionId))
                                        .thenReturn(savedOrder);
                            });
                });
    }

    public Flux<Order> getAll() {
        return orderRepository.findAll()
                .flatMap(this::attachItems);
    }

    public Mono<Order> getById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found")));
    }

    private Mono<Order> attachItems(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .map(items -> {
                    order.setItems(items);
                    return order;
                });
    }
}