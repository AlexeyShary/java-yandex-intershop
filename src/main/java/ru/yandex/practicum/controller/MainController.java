package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String rootRedirect() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(Model model,
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber) {

        var page = productService.getProducts(search, sort, pageNumber, pageSize);
        List<List<ItemViewDto>> items = partition(page.getContent(), 3);

        model.addAttribute("items", items);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", new Paging(pageNumber, pageSize, page.hasNext(), page.hasPrevious()));

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String updateFromMain(@PathVariable Long id, @RequestParam String action) {
        cartService.updateItem(id, action);
        return "redirect:/main/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        var product = productService.getById(id).orElseThrow();
        var itemView = productService.toDto(product);
        model.addAttribute("item", itemView);
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateFromItem(@PathVariable Long id, @RequestParam String action) {
        cartService.updateItem(id, action);
        return "redirect:/items/" + id;
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        return list.stream()
                .collect(Collectors.groupingBy(i -> list.indexOf(i) / size))
                .values()
                .stream()
                .toList();
    }

    public record Paging(int pageNumber, int pageSize, boolean hasNext, boolean hasPrevious) {}
}