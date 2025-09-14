package com.project.roomly.service;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.RoomPaymentInfoDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;


public interface RoomService {
    void saveRoom(RoomDto roomDto, MultipartFile[] media, String uuid) throws IOException;
    void deleteRoom(Long roomId, String uuid);
    void setRoom(SetRoomDto setRoomDto, String uuid);
    ResponseRoomMediaDto getRoom(Long roomId);
    SearchRoomsDto searchRoomsByDate(String city,
                                     BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     Integer page);
    ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId, Integer page);
    RoomPaymentInfoDto getRoomPaymentInfo(Long roomId);
    void addMedia(MultipartFile media, Long roomId, String uuid) throws IOException;
    void deleteMedia(String key, Long roomId, String uuid);

}
