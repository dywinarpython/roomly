package com.project.roomly.dto.Hotel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record SetHotelDto(
        @NotNull Long hotelId,
        @Size(max = 255, min = 1) String name,
        @Size(max = 255, min = 1) String address,
        @Max(value = 100, message = "The maximum prepayment percentage is 100") @Min(value = 0, message = "The minimum prepayment percentage is 0") Integer prepaymentPercentage) {
}

