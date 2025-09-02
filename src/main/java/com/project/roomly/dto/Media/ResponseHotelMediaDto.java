package com.project.roomly.dto.Media;

import com.project.roomly.dto.Hotel.ResponseHotelDto;

import java.util.List;

public record ResponseHotelMediaDto(ResponseHotelDto hotel, List<MediaDto> media) {
}
