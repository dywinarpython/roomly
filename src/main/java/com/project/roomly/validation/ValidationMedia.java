package com.project.roomly.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ValidationMedia {

    private  final List<String> typeFiles;

    public ValidationMedia(@Value("${storage.type_file}") String typeFile) {
        this.typeFiles = Arrays.stream(typeFile.split(", ")).toList();
        log.info("Допустимые типы media: {}", typeFiles);
    }

    public void validationTypeMedia(MultipartFile[] files){
        if(files.length > 10) throw new ValidationException("Максимальное количество файлов 10 (The maximum number of files is 10).");
        for (MultipartFile file: files){
            String typeFile = Objects.requireNonNull(file.getContentType()).substring(file.getContentType().indexOf("/") + 1);
            if(!typeFiles.contains(typeFile)){
                throw new ValidationException("Некорректный тип файла, допустимый форматы (Incorrect file type, acceptable formats): " + typeFiles);
            }
        }
    }
}
