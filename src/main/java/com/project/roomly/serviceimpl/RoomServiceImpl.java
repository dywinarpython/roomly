package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.Room;
import com.project.roomly.mapper.MapperRoom;
import com.project.roomly.repository.RoomRepository;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.MediaService;
import com.project.roomly.service.RoomService;
import jakarta.persistence.EntityManager;
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
}