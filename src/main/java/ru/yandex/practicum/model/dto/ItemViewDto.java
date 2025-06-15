package ru.yandex.practicum.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemViewDto {
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private int count;
    private BigDecimal price;
}