package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.ResponseHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.*;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.HotelMedia;
import com.project.roomly.mapper.MapperHotel;
import com.project.roomly.repository.HotelMediaRepository;
import com.project.roomly.repository.HotelRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import com.project.roomly.storage.service.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    private final HotelMediaRepository hotelMediaRepository;

    private final MapperHotel mapperHotel;

    private final MediaService mediaService;

    private final EntityManager entityManager;

    private final StorageService storageService;

    private final CacheManager cacheManager;

    @Value("${pageable.size}")
    private Integer pageableSize;



    @Override
    @Transactional
    public void saveHotel(RequestHotelDto requestHotelDto, MultipartFile[] files, String uuid) throws IOException {
        Hotel hotel = hotelRepository.save(mapperHotel.hotelDtoToHotel(requestHotelDto, uuid));
        List<String> keyMedia = storageService.uploadMedias(files);
        List<HotelMedia> mediaSet = keyMedia.stream().map(key -> new HotelMedia(key, hotel)).toList();
        hotelMediaRepository.saveAll(mediaSet);
    }
    @Override
    @Transactional
    @CacheEvict(cacheNames = "HOTEL", key = "#id")
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
    @CacheEvict(cacheNames = "HOTEL", key = "#setHotelDto.hotelId()")
    public void setHotel(SetHotelDto setHotelDto, String uuid) {
        checkOwnerHotel(setHotelDto.hotelId(), uuid);
        Hotel hotel = entityManager.getReference(Hotel.class, setHotelDto.hotelId());
        if(setHotelDto.prepaymentPercentage() == null && setHotelDto.name() == null && setHotelDto.address() == null){
            throw new ValidationException("Not a single field has been updated!");
        }
        mapperHotel.updateHotelField(setHotelDto, hotel);
    }

    @Override
    public void checkOwnerHotel(Long id, String uuid) {
        Cache cache = cacheManager.getCache("CHECK_OWNER_HOTEL");
        Boolean  exists;
        if(cache == null)  exists = hotelRepository.existsByIdAndOwner(id, UUID.fromString(uuid));
        else {
            exists = cache.get(id + ":" + uuid, Boolean.class);
            if(exists == null) {
                exists = hotelRepository.existsByIdAndOwner(id, UUID.fromString(uuid));
                cache.put(id + ":" + uuid, exists);
            }
        }
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
    @Cacheable(cacheNames = "HOTEL", key = "#hotelId")
    public ResponseHotelMediaDto getHotel(Long hotelId) {
        Optional<ResponseHotelDto> optionalResponseHotelDto = hotelRepository.findHotel(hotelId);
        if(optionalResponseHotelDto.isEmpty()){
            throw new NoSuchElementException("Hotel is not found!");
        }
        ResponseHotelDto responseHotelDto = optionalResponseHotelDto.get();
        List<MediaDto> mediaDtoList = mediaService.getMediaDtoByHotelId(hotelId);
        return new ResponseHotelMediaDto(responseHotelDto, mediaDtoList);
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseHotelsMediaDto getHotelsByOwner(String uuid, Integer page) {
        List<ResponseHotelDto> hotelList = hotelRepository.findHotelsByOwner(UUID.fromString(uuid), PageRequest.of(page, pageableSize));
        if(hotelList.isEmpty()) return new ResponseHotelsMediaDto(List.of());
        List<ResponseMediaDto> mediaList = mediaService.getMediaByHotelsId(hotelList.stream().map(ResponseHotelDto::id).toList());

        Map<Long, List<ResponseMediaDto>> allHotelsMedia = mediaList.stream().collect(Collectors.groupingBy(ResponseMediaDto::id, LinkedHashMap::new, Collectors.toList()));
        ResponseHotelsMediaDto responseHotelsMediaDto = new ResponseHotelsMediaDto(hotelList.stream().map(hotel -> new ResponseHotelMediaDto(
                hotel, allHotelsMedia.getOrDefault(hotel.id(), List.of()).stream().map(media -> new MediaDto(media.url())).toList()
        )).toList());

        return responseHotelsMediaDto;

    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "HOTEL", key = "#hotelId")
    public void addMedia(MultipartFile media, Long hotelId, String uuid) throws IOException {
        checkOwnerHotel(hotelId, uuid);
        Hotel hotel = entityManager.getReference(Hotel.class, hotelId);
        if(!(hotelMediaRepository.countMediaByHotel(hotelId) < 10)) {
            throw new ValidationException("Максимальное количество media у отеля 10 (The maximum number of media items for a hotel is 10).");
        }
        String key = storageService.uploadMedia(media);
        hotelMediaRepository.save(new HotelMedia(key, hotel));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "HOTEL", key = "#hotelId")
    public void deleteMedia(String key, Long hotelId, String uuid) {
        checkOwnerHotel(hotelId, uuid);
        int countUpdate = hotelMediaRepository.updateMediaHotel(key);
        if(countUpdate == 0) throw new NoSuchElementException("Медиа с таким ключом у отеля не найдена (Media with such a key was not found at the hotel).");
    }
}
