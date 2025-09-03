package com.project.roomly.controller;

import com.project.roomly.dto.Hotel.HotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import com.project.roomly.dto.Media.ResponseRoomsMediaDto;
import com.project.roomly.service.HotelService;
import com.project.roomly.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hotel")
@Tag(name = "Управление отелями")
public class HotelController {

    private final HotelService hotelService;

    private final RoomService roomService;


    @Operation(
            summary = "Создания отеля",
            responses = @ApiResponse(responseCode = "201")
    )
    @PostMapping
    public ResponseEntity<Void> createHotel(@Valid @RequestBody HotelDto hotelDto, @AuthenticationPrincipal Jwt jwt){
        hotelService.saveHotel(hotelDto, jwt.getSubject());
        return ResponseEntity.status(201).build();
    }

    @Operation(
            summary = "Удаление отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> createHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
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
