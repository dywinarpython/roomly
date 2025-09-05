package com.project.roomly.controller;

import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.dto.search.SearchDto;
import com.project.roomly.service.RoomService;
import com.project.roomly.validation.ValidationDateBookingService;
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
@Tag(name = "Управление номерами")
public class RoomController {

    private final RoomService roomService;

    private final ValidationDateBookingService validationDateBookingService;


    @Operation(
            summary = "Создания номера",
            responses = @ApiResponse(responseCode = "201")
    )
    @PostMapping
    public ResponseEntity<Void> createRoom(@Valid @RequestBody RoomDto roomDto, @AuthenticationPrincipal Jwt jwt){
        roomService.saveRoom(roomDto, jwt.getSubject());
        return ResponseEntity.status(201).build();
    }

    @Operation(
            summary = "Получения свободных номеров на определенную дату",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SearchRoomsDto.class)))
    )
    @PostMapping("/search")
    public ResponseEntity<SearchRoomsDto> getHotel(@Valid @RequestBody SearchDto searchDto){
        validationDateBookingService.checkDate(searchDto.startTime(), searchDto.endTime());
        return ResponseEntity.ok(roomService.searchRoomsByDate(searchDto));
    }


    @Operation(
            summary = "Удаление номера",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> createHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        roomService.deleteRoom(id, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "Room is deleted"));
    }


    @Operation(
            summary = "Получения информации номера",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseRoomMediaDto.class)))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseRoomMediaDto> getHotel(@PathVariable Long id){
        return ResponseEntity.ok(roomService.getRoom(id));
    }



    @Operation(
            summary = "Изменение номера",
            responses = @ApiResponse(responseCode = "200")
    )
    @PatchMapping
    public ResponseEntity<Map<String, String>> setRoom(@RequestBody SetRoomDto setRoomDto, @AuthenticationPrincipal Jwt jwt){
        roomService.setRoom(setRoomDto, jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "The changes were successful!"));
    }
}
