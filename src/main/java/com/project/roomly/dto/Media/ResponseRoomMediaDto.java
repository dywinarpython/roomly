package com.project.roomly.dto.Media;

import com.project.roomly.dto.Room.ResponseRoomDto;

import java.util.List;

public record ResponseRoomMediaDto(ResponseRoomDto room, List<MediaDto> media) {
}
