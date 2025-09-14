package com.project.roomly.service;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseMediaDto;

import java.util.List;

public interface MediaService {
    List<MediaDto> getMediaDtoByHotelId(Long hotelId);
    List<MediaDto> getMediaDtoByRoomId(Long roomId);
    List<ResponseMediaDto> getMediaByRoomsId(List<Long> roomsId);
    List<ResponseMediaDto> getMediaByHotelsId(List<Long> hotelsId);
}
