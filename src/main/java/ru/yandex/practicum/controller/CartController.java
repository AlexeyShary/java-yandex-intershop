package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart/items")
    public String viewCart(Model model) {
        List<ItemViewDto> items = cartService.getAll();

        model.addAttribute("items", items);
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("empty", items.isEmpty());

        return "cart";
    }

    @PostMapping("/cart/items/{id}")
    public String updateCart(@PathVariable Long id, @RequestParam String action) {
        cartService.updateItem(id, action);
        return "redirect:/cart/items";
    }
}