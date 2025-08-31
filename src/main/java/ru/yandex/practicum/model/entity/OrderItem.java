package ru.yandex.practicum.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    private Long id;

    private Long orderId;

    private Long productId;
    private String title;
    private String description;
    private String imgPath;
    private int count;
    private BigDecimal price;
}