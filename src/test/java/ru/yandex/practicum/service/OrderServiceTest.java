package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.model.entity.OrderItem;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class OrderServiceTest {
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private CartService cartService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        cartService = mock(CartService.class);
        orderService = new OrderService(orderRepository, orderItemRepository, cartService);
    }

    private ItemViewDto mockCartItem() {
        return ItemViewDto.builder()
                .id(1L)
                .title("Product")
                .description("Desc")
                .imgPath("/img.png")
                .price(BigDecimal.TEN)
                .count(2)
                .build();
    }

    @Test
    void testBuyItemsWithProducts() {
        when(cartService.getAll("session1"))
                .thenReturn(Flux.just(mockCartItem()));

        Order savedOrder = Order.builder()
                .id(100L)
                .totalSum(BigDecimal.valueOf(20))
                .build();

        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.empty());
        when(cartService.clearCart("session1")).thenReturn(Mono.empty());

        StepVerifier.create(orderService.buyItems("session1"))
                .expectNextMatches(order -> order.getId().equals(100L) && order.getTotalSum().compareTo(BigDecimal.valueOf(20)) == 0)
                .verifyComplete();
    }

    @Test
    void testBuyItemsEmptyCart() {
        when(cartService.getAll("session2")).thenReturn(Flux.empty());

        StepVerifier.create(orderService.buyItems("session2"))
                .verifyComplete(); // Должен вернуть Mono.empty()
    }

    @Test
    void testGetByIdExists() {
        Order order = Order.builder().id(5L).totalSum(BigDecimal.TEN).build();
        when(orderRepository.findById(5L)).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.getById(5L))
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void testGetByIdNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getById(99L))
                .expectErrorMatches(err -> err instanceof NotFoundException &&
                        err.getMessage().equals("Order not found"))
                .verify();
    }

    @Test
    void testGetAllWithItems() {
        Order order = Order.builder().id(1L).totalSum(BigDecimal.TEN).build();
        OrderItem item = OrderItem.builder().orderId(1L).productId(1L).title("P").description("D").count(1).price(BigDecimal.TEN).build();

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(item));

        StepVerifier.create(orderService.getAll())
                .expectNextMatches(o -> o.getId().equals(1L) && o.getItems().size() == 1)
                .verifyComplete();
    }
}