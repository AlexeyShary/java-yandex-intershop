package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.*;
import ru.yandex.practicum.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    @Transactional
    public Order buyItems() {
        List<ItemViewDto> cartItems = cartService.getAll();
        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        Order order = Order.builder().build();
        orderRepository.save(order);

        for (ItemViewDto item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(item.getId())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .imgPath(item.getImgPath())
                    .price(item.getPrice())
                    .count(item.getCount())
                    .build();

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getCount())));
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalSum(total);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        cartService.clearCart();
        return order;
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }
}