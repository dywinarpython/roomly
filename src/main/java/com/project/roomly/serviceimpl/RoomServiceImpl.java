package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.dto.Room.*;
import com.project.roomly.dto.Search.SearchDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Override
    @Transactional
    public void saveRoom(RoomDto roomDto, MultipartFile[] media, String uuid) throws IOException {
       hotelService.checkOwnerHotel(roomDto.hotelId(), uuid);
        Room room = roomRepository.save(mapperRoom.roomDtoToRoom(roomDto,entityManager.getReference(Hotel.class, roomDto.hotelId())));
       List<String> keyMedia = storageService.uploadMedia(media);
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
                setRoomDto.floor() == null && setRoomDto.priceDay() == null) {
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
    public SearchRoomsDto searchRoomsByDate(SearchDto searchDto) {
        return new SearchRoomsDto(roomRepository.findAvailableRooms(searchDto.startTime(), searchDto.endTime()));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId) {
        List<ResponseRoomDto> roomList = roomRepository.findRoomsByHotelId(hotelId);
        if (roomList.isEmpty()){
            return new ResponseRoomsMediaDto(List.of());
        }
        List<RoomsMediaDto> mediaList = mediaService.getMediaByRoomsId(roomList.stream().map(ResponseRoomDto::id).toList());

        Map<Long, List<RoomsMediaDto>> allRoomsMedia = mediaList.stream().collect(Collectors.groupingBy(RoomsMediaDto::roomId, LinkedHashMap::new, Collectors.toList()));
        return new ResponseRoomsMediaDto(roomList.stream().map(room -> new ResponseRoomMediaDto(
                new ResponseRoomDto(room.id(), room.name(), room.countRoom(), room.priceDay(), room.floor(), room.prepaymentPercentage(), room.hotelId()),
                allRoomsMedia.getOrDefault(room.id(), List.of()).stream().map(media -> new MediaDto(media.url())).toList()
        )).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomPaymentInfoDto getRoomPaymentInfo(Long roomId) {
        return roomRepository.findRoomPricing(roomId).orElseThrow(() -> new NoSuchElementException("Room is not found"));
    }

    @Transactional(readOnly = true)
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