package com.project.roomly.controller;

import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.RoomService;
import com.project.roomly.validation.ValidationMedia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hotel")
@Tag(name = "Управление отелями")
public class HotelController {

    private final HotelService hotelService;

    private final RoomService roomService;

    private final ValidationMedia validationMedia;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Создание отеля",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            encoding = {
                                    @Encoding(
                                            name = "hotel",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    )
                            }
                    )
            ),
            responses = @ApiResponse(responseCode = "201")
    )
    public ResponseEntity<Void> createHotel(
            @Valid @RequestPart("hotel") RequestHotelDto requestHotelDto,
            @RequestPart(value = "file", required = false)   MultipartFile[] media,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        validationMedia.validationTypeMedia(media);
        hotelService.saveHotel(requestHotelDto, media, jwt.getSubject());
        return ResponseEntity.status(201).build();
    }

    @PostMapping(value = "/{hotelId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Добавления media отеля",
            responses = @ApiResponse(responseCode = "201")
    )
    public ResponseEntity<Void> createMediaHotel(
            @PathVariable Long hotelId,
            @RequestPart(value = "file")   MultipartFile media,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        validationMedia.validationTypeMedia(new MultipartFile[]{media});
        hotelService.addMedia(media, hotelId, jwt.getSubject());
        return ResponseEntity.status(201).build();
    }


    @Operation(
            summary = "Удаление медиа отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}/")
    public ResponseEntity<Map<String, String>> deleteMediaHotel(@PathVariable Long id, @RequestParam("keyMedia") String keyMedia, @AuthenticationPrincipal Jwt jwt){
        hotelService.deleteMedia(keyMedia, id, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "Media is deleted"));
    }

    @Operation(
            summary = "Удаление отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        hotelService.deleteHotel(id, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "Hotel is deleted"));
    }

    @Operation(
            summary = "Получения информации отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseHotelMediaDto.class)))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHotelMediaDto> getHotel(@PathVariable Long id){
        return ResponseEntity.ok(hotelService.getHotel(id));
    }


    @Operation(
            summary = "Получения номеров отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseRoomsMediaDto.class)))
    )
    @GetMapping("rooms/")
    public ResponseEntity<ResponseRoomsMediaDto> getRooms(@RequestParam Long hotelId){
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
    }

    @Operation(
            summary = "Изменение отеля",
            responses = @ApiResponse(responseCode = "200")
    )
    @PatchMapping
    public ResponseEntity<Map<String, String>> setRoom(@RequestBody SetHotelDto setHotelDto, @AuthenticationPrincipal Jwt jwt){
        hotelService.setHotel(setHotelDto, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "The changes were successful!"));
    }
}
