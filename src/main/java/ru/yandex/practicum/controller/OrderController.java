package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/buy")
    public Mono<String> buy(WebSession webSession) {
        return orderService.buyItems(webSession.getId())
                .map(Order::getId)
                .map(id -> "redirect:/orders/" + id + "?newOrder=true");
    }

    @GetMapping("/orders")
    public Mono<String> allOrders(Model model) {
        Flux<Order> orders = orderService.getAll();
        return orders.collectList().flatMap(orderList -> {
            model.addAttribute("orders", orderList);
            return Mono.just("orders");
        });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> viewOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    ) {
        return orderService.getById(id)
                .flatMap(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return Mono.just("order");
                });
    }
}