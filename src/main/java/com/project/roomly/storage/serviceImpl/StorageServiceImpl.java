package com.project.roomly.storage.serviceImpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.entity.Media;
import com.project.roomly.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private final S3Client s3Client;

    private final String bucketName;

    private final String fileUrlTemplate;

    public StorageServiceImpl(S3Client s3Client,
                              @Value("${storage.bucket_name}") String bucketName,
                              @Value("${storage.file_url_template}") String fileUrlTemplate) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.fileUrlTemplate = fileUrlTemplate;
    }

    @Override
    public Set<Media> uploadMedia(MultipartFile[] files) throws IOException {
        List<String> lsKey = new ArrayList<>();
        for (MultipartFile file: files ){
            UUID nameMedia = UUID.randomUUID();
            String key = nameMedia + "." + Objects.requireNonNull(file.getContentType()).substring(file.getContentType().indexOf("/") + 1);
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            lsKey.add(key);
        }
        return lsKey.stream().map(url -> new Media(null, url)).collect(Collectors.toSet());
    }

    @Override
    public List<MediaDto> getMedias(List<String> nameMedia) {
        return nameMedia.stream().map(media -> new MediaDto(fileUrlTemplate + media)).toList();
    }

    @Override
    public String getMedia(String nameMedia) {
        return  fileUrlTemplate + nameMedia;
    }
}
