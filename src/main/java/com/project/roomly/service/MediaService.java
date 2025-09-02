package com.project.roomly.service;

import com.project.roomly.dto.Media.MediaDto;

import java.util.List;

public interface MediaService {
    List<MediaDto> getMediaDtoByHotelId(Long hotelId);
    List<MediaDto> getMediaDtoByRoomId(Long roomId);

}
