package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final String PLUS_ACTION = "PLUS";
    private static final String MINUS_ACTION = "MINUS";
    private static final String DELETE_ACTION = "DELETE";

    private final ProductRepository productRepository;

    private final Map<String, Map<Long, Integer>> carts = new ConcurrentHashMap<>();

    private Map<Long, Integer> getCart(String sessionId) {
        return carts.computeIfAbsent(sessionId, k -> new HashMap<>());
    }

    public Flux<ItemViewDto> getAll(String sessionId) {
        Map<Long, Integer> cart = getCart(sessionId);
        return Flux.fromIterable(cart.entrySet())
                .flatMap(entry ->
                        productRepository.findById(entry.getKey())
                                .map(product -> ItemViewDto.builder()
                                        .id(product.getId())
                                        .title(product.getTitle())
                                        .description(product.getDescription())
                                        .imgPath(product.getImgPath())
                                        .price(product.getPrice())
                                        .count(entry.getValue())
                                        .build()
                                )
                );
    }

    public Mono<String> getTotal(String sessionId) {
        return getAll(sessionId)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .map(BigDecimal::toPlainString);
    }

    public Mono<Integer> getCountForProduct(String sessionId, Long productId) {
        Map<Long, Integer> cart = getCart(sessionId);
        return Mono.just(cart.getOrDefault(productId, 0));
    }

    public Mono<Void> updateItem(String sessionId, Long productId, String action) {
        Map<Long, Integer> cart = getCart(sessionId);
        int count = cart.getOrDefault(productId, 0);

        switch (action.toUpperCase()) {
            case PLUS_ACTION -> cart.put(productId, count + 1);
            case MINUS_ACTION -> {
                if (count > 1) cart.put(productId, count - 1);
                else cart.remove(productId);
            }
            case DELETE_ACTION -> cart.remove(productId);
        }

        return Mono.empty();
    }

    public Mono<Void> clearCart(String sessionId) {
        carts.remove(sessionId);
        return Mono.empty();
    }
}