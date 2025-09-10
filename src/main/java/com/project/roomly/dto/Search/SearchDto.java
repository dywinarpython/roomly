package com.project.roomly.dto.Search;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SearchDto(
        @NotNull LocalDate startTime,
        @NotNull LocalDate endTime,
        // TODO: Добавить поиск по цене, больше или меньше при значении , morePrices
        BigDecimal priceDay,
        Boolean morePrices
        ) {
}
