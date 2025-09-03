package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.repository.MediaRepository;
import com.project.roomly.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    public List<MediaDto> getMediaDtoByHotelId(Long hotelId){
        return mediaRepository.findMediaByHotelId(hotelId);
    }

    @Override
    public List<MediaDto> getMediaDtoByRoomId(Long roomId) {
        return mediaRepository.findMediaByRoomId(roomId);
    }

    @Override
    public List<RoomsMediaDto> getMediaByRoomsId(List<Long> roomsId) {
        return mediaRepository.findMediasByRoomsId(roomsId);
    }

}
