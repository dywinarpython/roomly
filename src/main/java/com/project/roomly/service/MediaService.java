package com.project.roomly.service;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;

import java.util.List;

public interface MediaService {
    List<MediaDto> getMediaDtoByHotelId(Long hotelId);
    List<MediaDto> getMediaDtoByRoomId(Long roomId);
    List<RoomsMediaDto> getMediaByRoomsId(List<Long> roomsId);
}
