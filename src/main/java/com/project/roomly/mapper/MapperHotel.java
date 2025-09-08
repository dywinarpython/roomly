package com.project.roomly.mapper;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.Media;
import org.mapstruct.*;

import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperHotel {

    @Mappings(value = {
            @Mapping(target = "owner", expression = "java(stringToUUID(uuid))"),
            @Mapping(target = "media", expression = "java(media)")
    })
    Hotel hotelDtoToHotel(RequestHotelDto requestHotelDto, @Context String uuid, @Context Set<Media> media);

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
