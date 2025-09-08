package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.repository.MediaRepository;
import com.project.roomly.service.MediaService;
import com.project.roomly.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    private final StorageService storageService;

    public List<MediaDto> getMediaDtoByHotelId(Long hotelId){
        return storageService.getMedias(mediaRepository.findMediaByHotelId(hotelId));
    }

    @Override
    public List<MediaDto> getMediaDtoByRoomId(Long roomId) {
        return storageService.getMedias(mediaRepository.findMediaByRoomId(roomId));
    }

    @Override
    public List<RoomsMediaDto> getMediaByRoomsId(List<Long> roomsId) {
        return mediaRepository.findMediasByRoomsId(roomsId)
                .stream().map(roomsMediaDto ->
                        new RoomsMediaDto(roomsMediaDto.roomId(), storageService.getMedia(roomsMediaDto.url()))).toList();
    }

}
