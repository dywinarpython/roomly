package com.project.roomly.controller;

import com.project.roomly.dto.Hotel.HotelDto;
import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import com.project.roomly.service.HotelService;
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
        return ResponseEntity.ok(Map.of("message", "Room is deleted"));
    }

    @Operation(
            summary = "Получения информации отеля",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseHotelMediaDto.class)))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHotelMediaDto> getHotel(@PathVariable Long id){
        return ResponseEntity.ok(hotelService.getHotel(id));
    }
}
