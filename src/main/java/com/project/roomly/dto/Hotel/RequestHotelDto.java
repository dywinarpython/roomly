package com.project.roomly.dto.Hotel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record RequestHotelDto(
        @Size(max = 255, min = 1)
        @NotNull String name,
        @Size(max = 255, min = 1)
        @NotNull String address,
        @NotNull @Max(100) @Min(0) Integer prepaymentPercentage) {
}

