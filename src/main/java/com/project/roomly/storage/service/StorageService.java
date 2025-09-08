package com.project.roomly.storage.service;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.entity.Media;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface StorageService {
    Set<Media> uploadMedia(MultipartFile[] files) throws IOException;
    List<MediaDto> getMedias(List<String> nameMedia);
    String getMedia(String nameMedia);
}
