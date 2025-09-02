package com.project.roomly.controller;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Room.RoomDto;
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
@RequestMapping("/api/v1/room")
@Tag(name = "Управление комнатами")
public class RoomController {

    private final RoomService roomService;


    @Operation(
            summary = "Создания комнаты",
            responses = @ApiResponse(responseCode = "201")
    )
    @PostMapping
    public ResponseEntity<Void> createRoom(@Valid @RequestBody RoomDto roomDto, @AuthenticationPrincipal Jwt jwt){
        roomService.saveRoom(roomDto, jwt.getSubject());
        return ResponseEntity.status(201).build();
    }


    @Operation(
            summary = "Удаление комнаты",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> createHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        roomService.deleteRoom(id, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "Room is deleted"));
    }


    @Operation(
            summary = "Получения информации комнаты",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseRoomMediaDto.class)))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseRoomMediaDto> getHotel(@PathVariable Long id){
        return ResponseEntity.ok(roomService.getRoom(id));
    }
}
