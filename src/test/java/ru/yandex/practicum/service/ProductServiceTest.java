package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.model.SortType;
import ru.yandex.practicum.model.dto.ItemViewDto;
import ru.yandex.practicum.model.entity.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {
    private ProductRepository productRepository;
    private CartService cartService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        cartService = mock(CartService.class);
        productService = new ProductService(productRepository, cartService);
    }

    private Product product(long id, String title, String desc, BigDecimal price) {
        return Product.builder()
                .id(id)
                .title(title)
                .description(desc)
                .price(price)
                .imgPath("/img.png")
                .build();
    }

    @Test
    void testToDto() {
        Product p = product(1L, "T", "D", BigDecimal.ONE);
        ItemViewDto dto = productService.toDto(p, 3);

        assertEquals(1L, dto.getId());
        assertEquals("T", dto.getTitle());
        assertEquals(3, dto.getCount());
    }

    @Test
    void testGetById() {
        Product p = product(1L, "Title", "Desc", BigDecimal.TEN);
        when(productRepository.findById(1L)).thenReturn(Mono.just(p));

        StepVerifier.create(productService.getById(1L))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    void testGetTotalCountWithSearch() {
        Product p1 = product(1L, "Phone", "Cool device", BigDecimal.TEN);
        Product p2 = product(2L, "Laptop", "Powerful", BigDecimal.ONE);

        when(productRepository.findAll()).thenReturn(Flux.just(p1, p2));

        StepVerifier.create(productService.getTotalCount("phone"))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void testGetProductsWithPaginationAndSort() {
        Product p1 = product(1L, "Apple", "Fruit", BigDecimal.valueOf(10));
        Product p2 = product(2L, "Banana", "Fruit", BigDecimal.valueOf(5));

        when(productRepository.findAll()).thenReturn(Flux.just(p2, p1));
        when(cartService.getCountForProduct(anyString(), anyLong()))
                .thenReturn(Mono.just(1)); // mock count for each product

        StepVerifier.create(productService.getProducts(null, SortType.ALPHA, 1, 10, "s"))
                .expectNextMatches(dto -> dto.getTitle().equals("Apple"))
                .expectNextMatches(dto -> dto.getTitle().equals("Banana"))
                .verifyComplete();
    }

    @Test
    void testGetProductsWithSearchFilter() {
        Product p1 = product(1L, "Apple", "Fruit", BigDecimal.valueOf(10));
        Product p2 = product(2L, "Car", "Vehicle", BigDecimal.valueOf(5));

        when(productRepository.findAll()).thenReturn(Flux.just(p1, p2));
        when(cartService.getCountForProduct(anyString(), anyLong()))
                .thenReturn(Mono.just(0));

        StepVerifier.create(productService.getProducts("car", SortType.PRICE, 1, 10, "s"))
                .expectNextMatches(dto -> dto.getTitle().equals("Car"))
                .verifyComplete();
    }
}