package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Hotel.HotelDto;
import com.project.roomly.dto.Hotel.ResponseHotelDto;
import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import com.project.roomly.mapper.MapperHotel;
import com.project.roomly.repository.HotelRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    private final MapperHotel mapperHotel;

    private final MediaService mediaService;


    @Override
    @Transactional
    public void saveHotel(HotelDto hotelDto, String uuid) {
        hotelRepository.save(mapperHotel.hotelDtoToHotel(hotelDto, uuid));
    }

    @Override
    @Transactional
    public void deleteHotel(Long id, String uuid) {
        int countDelete = hotelRepository.deleteByIdAndOwner(id, UUID.fromString(uuid));
        if(countDelete == 0){
            if(hotelRepository.existsById(id)){
                throw new AccessDeniedException("Access is denied");
            } else {
                throw new NoSuchElementException("Hotel is not found!");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void checkOwnerHotel(Long id, String uuid) {
        boolean exists = hotelRepository.existsByIdAndOwner(id, UUID.fromString(uuid));
        if(!exists){
            if(hotelRepository.existsById(id)){
                throw new AccessDeniedException("Access is denied");
            } else {
                throw new NoSuchElementException("Hotel is not found!");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseHotelMediaDto getHotel(Long hotelId) {
        Optional<ResponseHotelDto> optionalResponseHotelDto = hotelRepository.findHotel(hotelId);
        if(optionalResponseHotelDto.isEmpty()){
            throw new NoSuchElementException("Hotel is not found!");
        }
        ResponseHotelDto responseHotelDto = optionalResponseHotelDto.get();
        List<MediaDto> mediaDtoList = mediaService.getMediaDtoByHotelId(hotelId);
        return new ResponseHotelMediaDto(responseHotelDto, mediaDtoList);
    }
}
