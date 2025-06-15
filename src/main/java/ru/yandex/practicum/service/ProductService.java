package ru.yandex.practicum.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.Product;
import ru.yandex.practicum.model.SortType;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final HttpSession session;

    public Page<ItemViewDto> getProducts(String search, SortType sort, int page, int size) {
        List<Product> filtered = productRepository.findAll().stream()
                .filter(p -> search == null || search.isBlank()
                        || p.getTitle().toLowerCase().contains(search.toLowerCase())
                        || p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        switch (sort) {
            case ALPHA -> filtered.sort(Comparator.comparing(Product::getTitle));
            case PRICE -> filtered.sort(Comparator.comparing(Product::getPrice));
        }

        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<Product> pageContent = filtered.subList(from, to);

        List<ItemViewDto> views = pageContent.stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(views, PageRequest.of(page - 1, size), filtered.size());
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public ItemViewDto toDto(Product product) {
        Map<Long, Integer> cart = session.getAttribute("cart") == null
                ? new HashMap<>()
                : (Map<Long, Integer>) session.getAttribute("cart");

        int count = cart.getOrDefault(product.getId(), 0);

        return ItemViewDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .imgPath(product.getImgPath())
                .price(product.getPrice())
                .count(count)
                .build();
    }
}