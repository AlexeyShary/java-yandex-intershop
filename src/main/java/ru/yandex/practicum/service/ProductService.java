package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.Product;
import ru.yandex.practicum.model.SortType;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CartService cartService;

    public Flux<ItemViewDto> getProducts(String search, SortType sort, int page, int size, String sessionId) {
        Flux<Product> all = productRepository.findAll();

        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            all = all.filter(p ->
                    p.getTitle().toLowerCase().contains(lower) ||
                            p.getDescription().toLowerCase().contains(lower)
            );
        }

        switch (sort) {
            case ALPHA -> all = all.sort(Comparator.comparing(Product::getTitle));
            case PRICE -> all = all.sort(Comparator.comparing(Product::getPrice));
        }

        return all.skip((long) (page - 1) * size)
                .take(size)
                .flatMap(product -> cartService.getCountForProduct(sessionId, product.getId())
                        .map(count -> toDto(product, count)));
    }

    public Mono<Long> getTotalCount(String search) {
        Flux<Product> all = productRepository.findAll();

        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            all = all.filter(p ->
                    p.getTitle().toLowerCase().contains(lower) ||
                            p.getDescription().toLowerCase().contains(lower)
            );
        }

        return all.count();
    }

    public Mono<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public ItemViewDto toDto(Product product, int count) {
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