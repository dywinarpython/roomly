package com.project.roomly.service;

import com.project.roomly.dto.Hotel.HotelDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;

public interface HotelService {
    void saveHotel(HotelDto hotelDto, String uuid);
    void deleteHotel(Long id, String uuid);
    void checkOwnerHotel(Long id, String uuid);
    ResponseHotelMediaDto getHotel(Long id);
}
