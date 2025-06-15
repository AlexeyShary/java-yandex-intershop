package ru.yandex.practicum.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final String PLUS_ACTION = "PLUS";
    private static final String MINUS_ACTION = "MINUS";
    private static final String DELETE_ACTION = "DELETE";

    private final ProductRepository productRepository;
    private final HttpSession session;

    private Map<Long, Integer> getSessionCart() {
        Object raw = session.getAttribute("cart");
        if (raw == null) {
            Map<Long, Integer> newCart = new HashMap<>();
            session.setAttribute("cart", newCart);
            return newCart;
        }
        return (Map<Long, Integer>) raw;
    }

    public List<ItemViewDto> getAll() {
        Map<Long, Integer> cart = getSessionCart();
        List<ItemViewDto> result = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow();
            result.add(ItemViewDto.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .imgPath(product.getImgPath())
                    .price(product.getPrice())
                    .count(entry.getValue())
                    .build());
        }

        return result;
    }

    public BigDecimal getTotal() {
        return getAll().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateItem(Long productId, String action) {
        Map<Long, Integer> cart = getSessionCart();
        int count = cart.getOrDefault(productId, 0);

        switch (action.toUpperCase()) {
            case PLUS_ACTION -> cart.put(productId, count + 1);
            case MINUS_ACTION -> {
                if (count > 1) cart.put(productId, count - 1);
                else cart.remove(productId);
            }
            case DELETE_ACTION -> cart.remove(productId);
        }

        session.setAttribute("cart", cart);
    }

    public void clearCart() {
        session.removeAttribute("cart");
    }
}