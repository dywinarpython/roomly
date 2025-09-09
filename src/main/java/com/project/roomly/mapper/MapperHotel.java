package com.project.roomly.mapper;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.entity.Hotel;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperHotel {


    @Mapping(target = "owner", expression = "java(stringToUUID(uuid))")
    Hotel hotelDtoToHotel(RequestHotelDto requestHotelDto, @Context String uuid);

    @Named("stringToUUID")
    default UUID stringToUUID(@Context String uuid){
        return UUID.fromString(uuid);
    }

    @Mappings({
            @Mapping(target = "name", source = "name", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE),
            @Mapping(target = "address", source = "address", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE),
            @Mapping(target = "prepaymentPercentage", source = "prepaymentPercentage", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    })
    void updateHotelField(SetHotelDto setHotelDto, @MappingTarget Hotel hotel);
}
