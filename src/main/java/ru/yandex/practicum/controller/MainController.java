package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.SortType;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ProductService productService;
    private final CartService cartService;

    @GetMapping("/")
    public Mono<String> rootRedirect() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            WebSession webSession,
            Model model
    ) {
        Flux<ItemViewDto> itemsFlux = productService.getProducts(search, sort, pageNumber, pageSize, webSession.getId());
        Mono<Long> totalMono = productService.getTotalCount(search);

        return itemsFlux.collectList().zipWith(totalMono)
                .flatMap(tuple -> {
                    List<ItemViewDto> items = tuple.getT1();
                    Long total = tuple.getT2();

                    List<List<ItemViewDto>> partitioned = partition(items, 3);

                    boolean hasNext = pageNumber * pageSize < total;
                    boolean hasPrevious = pageNumber > 1;

                    model.addAttribute("items", partitioned);
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", new Paging(pageNumber, pageSize, hasNext, hasPrevious));

                    return Mono.just("main");
                });
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> updateFromMain(
            @PathVariable Long id,
            WebSession webSession,
            ServerWebExchange exchange
    ) {
        return exchange.getFormData()
                .flatMap(form -> {
                    String action = form.getFirst("action");
                    return cartService.updateItem(webSession.getId(), id, action);
                })
                .thenReturn("redirect:/items/" + id);
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(
            @PathVariable Long id,
            WebSession webSession,
            Model model
    ) {
        return productService.getById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Not found")))
                .flatMap(product -> cartService.getCountForProduct(webSession.getId(), product.getId())
                        .map(count -> productService.toDto(product, count)))
                .flatMap(itemView -> {
                    model.addAttribute("item", itemView);
                    return Mono.just("item");
                });
    }

    @PostMapping(path = "/items/{id}")
    public Mono<String> updateFromItem(
            @PathVariable Long id,
            WebSession webSession,
            ServerWebExchange exchange
    ) {
        return exchange.getFormData()
                .flatMap(form -> {
                    String action = form.getFirst("action");
                    return cartService.updateItem(webSession.getId(), id, action);
                })
                .thenReturn("redirect:/items/" + id);
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        return list.stream()
                .collect(Collectors.groupingBy(i -> list.indexOf(i) / size))
                .values()
                .stream()
                .toList();
    }

    public record Paging(int pageNumber, int pageSize, boolean hasNext, boolean hasPrevious) {
    }
}