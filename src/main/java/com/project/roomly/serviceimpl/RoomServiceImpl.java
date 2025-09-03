package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.Room;
import com.project.roomly.mapper.MapperRoom;
import com.project.roomly.repository.RoomRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import com.project.roomly.service.RoomService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    private final MapperRoom mapperRoom;

    private final HotelService hotelService;

    private final EntityManager entityManager;

    private final MediaService mediaService;

    @Override
    @Transactional
    public void saveRoom(RoomDto roomDto, String uuid) {
        hotelService.checkOwnerHotel(roomDto.hotelId(), uuid);
        Room room = mapperRoom.roomDtoToRoom(roomDto);
        Hotel hotel = entityManager.getReference(Hotel.class, roomDto.hotelId());
        room.setHotel(hotel);
        roomRepository.save(room);
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
    public ResponseRoomsMediaDto getRoomsByHotelId(Long hotelId) {
        List<ResponseRoomDto> roomList = roomRepository.findRoomsByHotelId(hotelId);
        if (roomList.isEmpty()){
            return new ResponseRoomsMediaDto(List.of());
        }
        List<RoomsMediaDto> mediaList = mediaService.getMediaByRoomsId(roomList.stream().map(ResponseRoomDto::id).toList());

        Map<Long, List<RoomsMediaDto>> allRoomsMedia = mediaList.stream().collect(Collectors.groupingBy(RoomsMediaDto::roomId, LinkedHashMap::new, Collectors.toList()));
        return new ResponseRoomsMediaDto(roomList.stream().map(room -> new ResponseRoomMediaDto(
                new ResponseRoomDto(room.id(), room.name(), room.countRoom(), room.priceDay(), room.floor()),
                allRoomsMedia.getOrDefault(room.id(), List.of()).stream().map(media -> new MediaDto(media.url(), media.mediaType())).toList()
        )).toList());
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