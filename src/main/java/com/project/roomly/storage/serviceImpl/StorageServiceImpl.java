package com.project.roomly.storage.serviceImpl;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;

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
    public List<String> uploadMedias(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new IOException("Файлы не переданы (No files provided).");
        }

        List<String> lsKey = new ArrayList<>();

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.contains("/")) {
                throw new IOException("Unsupported or missing content type");
            }
            String ext = contentType.substring(contentType.indexOf("/") + 1).trim().toLowerCase();
            if (ext.isEmpty()) {
                throw new IOException("Unsupported file extension derived from content type");
            }
            String key = UUID.randomUUID() + "." + ext;
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            lsKey.add(key);
        }

        return lsKey;
    }


    @Override
    public String uploadMedia(MultipartFile file) throws IOException {
        UUID nameMedia = UUID.randomUUID();
        String key = nameMedia + "." + Objects.requireNonNull(file.getContentType()).substring(file.getContentType().indexOf("/") + 1);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        return key;
    }

    @Override
    public void deleteMedias(List<String> keyMedia) {
        for (String key: keyMedia){
            s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                    .build()
            );
        }
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
