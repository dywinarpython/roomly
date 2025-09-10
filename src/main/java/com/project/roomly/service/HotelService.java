package com.project.roomly.service;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface HotelService {
    void saveHotel(RequestHotelDto requestHotelDto, MultipartFile[] files, String uuid) throws IOException;
    void deleteHotel(Long id, String uuid);
    void setHotel(SetHotelDto setHotelDto, String uuid);
    void checkOwnerHotel(Long id, String uuid);
    ResponseHotelMediaDto getHotel(Long hotelId);
    void addMedia(MultipartFile media, Long hotelId, String uuid) throws IOException;
    void deleteMedia(String key, Long hotelId, String uuid);
}
