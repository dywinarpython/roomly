package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.repository.HotelMediaRepository;
import com.project.roomly.repository.RoomMediaRepository;
import com.project.roomly.service.MediaService;
import com.project.roomly.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final RoomMediaRepository roomMediaRepository;

    private final HotelMediaRepository hotelMediaRepository;

    private final StorageService storageService;

    public List<MediaDto> getMediaDtoByHotelId(Long hotelId){
        return storageService.getMedias(hotelMediaRepository.findMediaKeyByHotelId(hotelId));
    }

    @Override
    public List<MediaDto> getMediaDtoByRoomId(Long roomId) {
        return storageService.getMedias(roomMediaRepository.findMediaByRoomId(roomId));
    }

    @Override
    public List<RoomsMediaDto> getMediaByRoomsId(List<Long> roomsId) {
        return roomMediaRepository.findMediasByRoomsId(roomsId)
                .stream().map(roomsMediaDto ->
                        new RoomsMediaDto(roomsMediaDto.roomId(), storageService.getMedia(roomsMediaDto.url()))).toList();
    }


}
