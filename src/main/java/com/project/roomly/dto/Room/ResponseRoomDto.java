package com.project.roomly.dto.Room;


import java.math.BigDecimal;

public record ResponseRoomDto(Long id,
                              String name,
                              String description,
                              Integer countRoom,
                              BigDecimal priceDay,
                              Integer floor,
                              Integer prepaymentPercentage,
                              Long hotelId) {

}
