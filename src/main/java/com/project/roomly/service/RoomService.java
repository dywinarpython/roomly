package com.project.roomly.service;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.RoomPaymentInfoDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.dto.Search.SearchDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface RoomService {
    void saveRoom(RoomDto roomDto, MultipartFile[] media, String uuid) throws IOException;
    void deleteRoom(Long roomId, String uuid);
    void setRoom(SetRoomDto setRoomDto, String uuid);
    ResponseRoomMediaDto getRoom(Long roomId);
    SearchRoomsDto searchRoomsByDate(SearchDto searchDto);
    ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId);
    RoomPaymentInfoDto getRoomPaymentInfo(Long roomId);

}
