package com.project.roomly.scheduler;

import com.project.roomly.repository.HotelMediaRepository;
import com.project.roomly.repository.RoomMediaRepository;
import com.project.roomly.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaScheduler {

    private final HotelMediaRepository hotelMediaRepository;

    private final RoomMediaRepository roomMediaRepository;

    private final StorageService storageService;

    @Transactional
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void deleteMediaHotel(){
        List<String> keyMedia =  hotelMediaRepository.findMediaForDelete();
        storageService.deleteMedias(keyMedia);
        int countDelete = hotelMediaRepository.deleteByHotelIsNull();
        log.info("Удалено media отелей: {}, было получено: {}", countDelete, keyMedia.size());

    }

    @Transactional
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void deleteMediaRoom(){
        List<String> keyMedia =  roomMediaRepository.findMediaForDelete();
        storageService.deleteMedias(keyMedia);
        int countDelete = roomMediaRepository.deleteByRoomIsNull();
        log.info("Удалено media комнат: {}, было получено: {}", countDelete, keyMedia.size());
    }
}
