package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<String> viewCart(WebSession webSession, Model model) {
        Flux<ItemViewDto> itemsFlux = cartService.getAll(webSession.getId());
        Mono<String> totalMono = cartService.getTotal(webSession.getId());

        return itemsFlux.collectList().flatMap(items -> {
            model.addAttribute("items", new ReactiveDataDriverContextVariable(Flux.fromIterable(items), 1));
            model.addAttribute("empty", items.isEmpty());
            return totalMono.map(total -> {
                model.addAttribute("total", total);
                return "cart";
            });
        });
    }

    @PostMapping(path = "/cart/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> updateCart(@PathVariable Long id, WebSession webSession, ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(form -> {
                    String action = form.getFirst("action");
                    return cartService.updateItem(webSession.getId(), id, action);
                })
                .thenReturn("redirect:/cart/items");
    }
}