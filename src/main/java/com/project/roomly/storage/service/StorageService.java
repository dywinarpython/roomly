package com.project.roomly.storage.service;

import com.project.roomly.dto.Media.MediaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    List<String> uploadMedias(MultipartFile[] files) throws IOException;
    String uploadMedia(MultipartFile file) throws IOException;
    void deleteMedias(List<String> keyMedia);
    List<MediaDto> getMedias(List<String> nameMedia);
    String getMedia(String nameMedia);
}
