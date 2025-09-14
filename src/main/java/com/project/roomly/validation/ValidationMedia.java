package com.project.roomly.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ValidationMedia {

    private  final List<String> typeFiles;

    private final Tika tika;

    public ValidationMedia(@Value("${storage.type_file}") String typeFile, Tika tika) {
        this.typeFiles = Arrays.stream(typeFile.split(", ")).map(s -> s.trim().toLowerCase())
                .toList();
        this.tika = tika;
        log.info("Допустимые типы media: {}", typeFiles);
    }

    public void validationTypeMedia(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new ValidationException("Файлы не переданы (No files provided).");
        }
        if (files.length > 10) {
            throw new ValidationException("Максимальное количество файлов 10 (The maximum number of files is 10).");
        }

        for (MultipartFile file : files) {
            if(file == null) throw new ValidationException("Ошибка обработки файла (File processing error).");
            String detectedType = tika.detect(file.getInputStream());
            String typeFile = detectedType.substring(detectedType.indexOf('/') + 1).toLowerCase();
            if (!typeFiles.contains(typeFile)) {
                throw new ValidationException(
                        "Некорректный тип файла, допустимый форматы (Incorrect file type, acceptable formats): " + typeFiles
                );
            }
        }
    }

}
