package com.project.roomly.mapper;

import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperRoom {

    Room roomDtoToRoom(RoomDto roomDto);
}
