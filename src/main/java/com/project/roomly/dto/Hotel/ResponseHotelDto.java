package com.project.roomly.dto.Hotel;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record ResponseHotelDto(Long id, String name, String address) {

}
