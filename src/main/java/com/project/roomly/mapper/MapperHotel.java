package com.project.roomly.mapper;

import com.project.roomly.dto.Hotel.HotelDto;
import com.project.roomly.entity.Hotel;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperHotel {

    @Mapping(target = "owner", expression = "java(stringToUUID(uuid))")
    Hotel hotelDtoToHotel(HotelDto hotelDto, @Context String uuid);

    @Named("stringToUUID")
    default UUID stringToUUID(@Context String uuid){
        return UUID.fromString(uuid);
    }
}
