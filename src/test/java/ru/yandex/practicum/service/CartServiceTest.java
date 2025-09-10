package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.model.entity.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

class CartServiceTest {
    private CartService cartService;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        cartService = new CartService(productRepository);
    }

    private Product buildTestProduct() {
        return Product.builder()
                .id(1L)
                .title("Test Product")
                .description("Desc")
                .price(BigDecimal.TEN)
                .imgPath("/img.png")
                .build();
    }

    @Test
    void testAddItemToCartAndGetAll() {
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(buildTestProduct()));

        cartService.updateItem("session1", 1L, "PLUS").block();

        StepVerifier.create(cartService.getAll("session1"))
                .expectNextMatches(item ->
                        item.getId().equals(1L)
                                && item.getTitle().equals("Test Product")
                                && item.getCount() == 1)
                .verifyComplete();
    }

    @Test
    void testGetTotalPrice() {
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(buildTestProduct()));

        cartService.updateItem("s1", 1L, "PLUS").block();
        cartService.updateItem("s1", 1L, "PLUS").block();

        StepVerifier.create(cartService.getTotal("s1"))
                .expectNext("20")
                .verifyComplete();
    }

    @Test
    void testDeleteFromCart() {
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(buildTestProduct()));

        cartService.updateItem("s2", 1L, "PLUS").block();
        cartService.updateItem("s2", 1L, "DELETE").block();

        StepVerifier.create(cartService.getAll("s2"))
                .verifyComplete();
    }

    @Test
    void testClearCart() {
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(buildTestProduct()));

        cartService.updateItem("s3", 1L, "PLUS").block();
        cartService.clearCart("s3").block();

        StepVerifier.create(cartService.getAll("s3"))
                .verifyComplete();
    }

    @Test
    void testGetCountForProduct() {
        cartService.updateItem("s4", 1L, "PLUS").block();
        cartService.updateItem("s4", 1L, "PLUS").block();

        StepVerifier.create(cartService.getCountForProduct("s4", 1L))
                .expectNext(2)
                .verifyComplete();
    }
}