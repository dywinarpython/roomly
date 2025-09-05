package com.project.roomly.service;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.RoomPaymentInfoDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.dto.search.SearchDto;


public interface RoomService {
    void saveRoom(RoomDto roomDto, String uuid);
    void deleteRoom(Long roomId, String uuid);
    void setRoom(SetRoomDto setRoomDto, String uuid);
    ResponseRoomMediaDto getRoom(Long roomId);
    SearchRoomsDto searchRoomsByDate(SearchDto searchDto);
    ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId);
    RoomPaymentInfoDto getRoomPaymentInfo(Long roomId);

}
