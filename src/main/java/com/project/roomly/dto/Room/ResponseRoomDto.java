package com.project.roomly.dto.Room;


import java.math.BigDecimal;

public record ResponseRoomDto(Long id, String name, Integer countRoom, BigDecimal priceDay, Integer floor) {

}
