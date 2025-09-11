package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Media.ResponseMediaDto;
import com.project.roomly.dto.Room.*;
import com.project.roomly.entity.*;
import com.project.roomly.mapper.MapperRoom;
import com.project.roomly.repository.RoomMediaRepository;
import com.project.roomly.repository.RoomRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import com.project.roomly.service.RoomService;
import com.project.roomly.storage.service.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    private final RoomMediaRepository roomMediaRepository;

    private final MapperRoom mapperRoom;

    private final HotelService hotelService;

    private final EntityManager entityManager;

    private final MediaService mediaService;

    private final StorageService storageService;

    @Value("${pageable.size}")
    private Integer pageableSize;

    @Override
    @Transactional
    public void saveRoom(RoomDto roomDto, MultipartFile[] media, String uuid) throws IOException {
       hotelService.checkOwnerHotel(roomDto.hotelId(), uuid);
        Room room = roomRepository.save(mapperRoom.roomDtoToRoom(roomDto,entityManager.getReference(Hotel.class, roomDto.hotelId())));
       List<String> keyMedia = storageService.uploadMedias(media);
       List<RoomMedia> mediaList = keyMedia.stream().map(key -> new RoomMedia(key, room)).toList();
       roomMediaRepository.saveAll(mediaList);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id, String uuid) {
        int countDelete = roomRepository.deleteByIdAndHotelOwner(id, UUID.fromString(uuid));
        if(countDelete == 0){
            if(roomRepository.existsById(id)){
                throw new AccessDeniedException("Access is denied");
            } else {
                throw new NoSuchElementException("Room is not found!");
            }
        }
    }

    @Override
    @Transactional
    public void setRoom(SetRoomDto setRoomDto, String uuid) {
        checkOwnerByRoomId(setRoomDto.roomId(), uuid);
        Room room = entityManager.getReference(Room.class, setRoomDto.roomId());
        if (setRoomDto.countRoom() == null && setRoomDto.name() == null &&
                setRoomDto.floor() == null && setRoomDto.priceDay() == null && setRoomDto.description() == null) {
            throw new ValidationException("Not a single field has been updated!");
        }
        mapperRoom.updateRoomField(setRoomDto, room);
    }

    // TODO: добавить кеширование
    @Override
    @Transactional(readOnly = true)
    public ResponseRoomMediaDto getRoom(Long roomId) {
        Optional<ResponseRoomDto> optionalResponseRoomDto = roomRepository.findRoom(roomId);
        if(optionalResponseRoomDto.isEmpty()){
            throw new NoSuchElementException("Room is not found!");
        }
        ResponseRoomDto responseHotelDto = optionalResponseRoomDto.get();
        List<MediaDto> mediaDtoList = mediaService.getMediaDtoByRoomId(roomId);
        return new ResponseRoomMediaDto(responseHotelDto, mediaDtoList);
    }

    @Override
    public SearchRoomsDto searchRoomsByDate(String city, BigDecimal minPrice, BigDecimal maxPrice, LocalDate startDate, LocalDate endDate, Integer page) {
        return new SearchRoomsDto(roomRepository.findAvailableRooms(city, minPrice, maxPrice, startDate, endDate, PageRequest.of(page, pageableSize)));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId, Integer page) {
        List<ResponseRoomDto> roomList = roomRepository.findRoomsByHotelId(hotelId, PageRequest.of(page, pageableSize));
        if (roomList.isEmpty()){
            return new ResponseRoomsMediaDto(List.of());
        }
        List<ResponseMediaDto> mediaList = mediaService.getMediaByRoomsId(roomList.stream().map(ResponseRoomDto::id).toList());

        Map<Long, List<ResponseMediaDto>> allRoomsMedia = mediaList.stream().collect(Collectors.groupingBy(ResponseMediaDto::id, LinkedHashMap::new, Collectors.toList()));
        return new ResponseRoomsMediaDto(roomList.stream().map(room -> new ResponseRoomMediaDto(
                new ResponseRoomDto(room.id(), room.name(), room.description(), room.countRoom(), room.priceDay(), room.floor(), room.prepaymentPercentage(), room.hotelId()),
                allRoomsMedia.getOrDefault(room.id(), List.of()).stream().map(media -> new MediaDto(media.url())).toList()
        )).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomPaymentInfoDto getRoomPaymentInfo(Long roomId) {
        return roomRepository.findRoomPricing(roomId).orElseThrow(() -> new NoSuchElementException("Room is not found"));
    }



    @Override
    @Transactional
    public void addMedia(MultipartFile media, Long roomId, String uuid) throws IOException {
        checkOwnerByRoomId(roomId, uuid);
        Room room = entityManager.getReference(Room.class, roomId);
        if(!(roomMediaRepository.countMediaByRoom(roomId) < 10)) {
            throw new ValidationException("Максимальное количество media у номера 10 (The maximum number of media items for a room is 10).");
        }
        String key = storageService.uploadMedia(media);
        roomMediaRepository.save(new RoomMedia(key, room));
    }

    @Override
    @Transactional
    public void deleteMedia(String key, Long roomId, String uuid) {
        checkOwnerByRoomId(roomId, uuid);
        int countUpdate = roomMediaRepository.updateMediaRoom(key);
        if(countUpdate == 0) throw new NoSuchElementException("Медиа с таким ключом у комнаты не найдена (Media with such a key was not found at the room).");
    }


    private void checkOwnerByRoomId(Long roomId, String uuid) {
        boolean exists = roomRepository.existsByRoomIdAndOwner(roomId, UUID.fromString(uuid));
        if(!exists){
            if(roomRepository.existsById(roomId)){
                throw new AccessDeniedException("Access is denied");
            } else {
                throw new NoSuchElementException("Room is not found!");
            }
        }
    }
}