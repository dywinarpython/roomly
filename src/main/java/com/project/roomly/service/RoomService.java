package com.project.roomly.service;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Room.RoomDto;

public interface RoomService {
    void saveRoom(RoomDto roomDto, String uuid);
    void deleteRoom(Long roomId, String uuid);
    ResponseRoomMediaDto getRoom(Long roomId);
}
