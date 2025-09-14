package com.project.roomly.dto.Room;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(hidden = true)
public record RoomPaymentInfoDto(BigDecimal priceDay, Integer prepaymentPercentage) {
}
