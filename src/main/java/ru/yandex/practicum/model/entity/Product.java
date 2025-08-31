package ru.yandex.practicum.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private Long id;

    private BigDecimal price;
    private String title;
    private String description;
    private String imgPath;
}