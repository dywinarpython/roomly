package com.project.roomly.dto.Room;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RoomDto(@NotNull @Size(max = 255, min = 1, message = "The name must contain a minimum of 1 and a maximum of 255 characters.") String name,
                      @NotNull Long hotelId,
                      @NotNull @Size(max = 255, min = 10, message = "The description must contain a minimum of 10 and a maximum of 255 characters.") String description,
                      @NotNull @Min(value = 1, message = "The minimum count room of accommodation is 1") @Max(value = 500, message = "The maximum count room of accommodation is 500") Integer countRoom,
                      @NotNull @Min(value = 0, message = "The minimum floor of accommodation is 0") @Max(value = 150, message = "The maximum floor of accommodation is 150") Integer floor,
                      @NotNull @Min(value = 1, message = "The price cannot be less than or equal to 0.") BigDecimal priceDay) {
}
