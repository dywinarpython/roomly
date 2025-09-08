package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.ResponseHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.*;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.Media;
import com.project.roomly.mapper.MapperHotel;
import com.project.roomly.repository.HotelRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import com.project.roomly.storage.service.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    private final MapperHotel mapperHotel;

    private final MediaService mediaService;

    private final EntityManager entityManager;

    private final StorageService storageService;



    @Override
    @Transactional
    public void saveHotel(RequestHotelDto requestHotelDto, MultipartFile[] files,  String uuid) throws IOException {
        Set<Media> mediaSet = storageService.uploadMedia(files);
        hotelRepository.save(mapperHotel.hotelDtoToHotel(requestHotelDto, uuid, mediaSet));
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
    @Transactional
    public void setHotel(SetHotelDto setHotelDto, String uuid) {
        checkOwnerHotel(setHotelDto.hotelId(), uuid);
        Hotel hotel = entityManager.getReference(Hotel.class, setHotelDto.hotelId());
        if(setHotelDto.prepaymentPercentage() == null && setHotelDto.name() == null && setHotelDto.address() == null){
            throw new ValidationException("Not a single field has been updated!");
        }
        mapperHotel.updateHotelField(setHotelDto, hotel);
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
