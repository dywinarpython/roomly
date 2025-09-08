package com.project.roomly.mapper;

import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.Media;
import com.project.roomly.entity.Room;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface MapperRoom {

    @Mappings(value = {
            @Mapping(target = "hotel", expression = "java(hotel)"),
            @Mapping(target = "media", expression = "java(media)")
    })
    Room roomDtoToRoom(RoomDto roomDto, @Context Hotel hotel, @Context Set<Media> media);

    @Mappings({
            @Mapping(target = "countRoom", source = "countRoom", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE),
            @Mapping(target = "name", source = "name", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE),
            @Mapping(target = "floor", source = "floor", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE),
            @Mapping(target = "priceDay", source = "priceDay", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    })
    void updateRoomField(SetRoomDto setRoomDto, @MappingTarget Room room);
}
