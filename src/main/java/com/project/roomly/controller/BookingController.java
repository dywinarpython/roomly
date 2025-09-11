package com.project.roomly.controller;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.dto.Booking.ResponseBookingsDto;
import com.project.roomly.service.BookingService;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/booking")
@Tag(name = "Управление бронированием")
public class BookingController {

    private final BookingService bookingService;

    private final ValidationDateBookingService validationDateBookingService;


    @Operation(
            summary = "Создания бронирования",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ResponseBookingDto.class)))
    )
    @PostMapping
    public ResponseEntity<ResponseBookingDto> createBooking(@Valid @RequestBody BookingDto bookingDto, @AuthenticationPrincipal Jwt jwt){
        validationDateBookingService.checkDate(bookingDto.startTime(), bookingDto.endTime());
        return ResponseEntity.status(201).body(bookingService.createBooking(bookingDto, jwt.getSubject()));
    }


    @Operation(
            summary = "Получение бронирования",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ResponseBookingDto.class)))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBookingDto> getBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.status(201).body(bookingService.getBooking(id, jwt.getSubject()));
    }

    @Operation(
            summary = "Получение всех бронирований пользователя",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ResponseBookingsDto.class)))
    )
    @GetMapping
    public ResponseEntity<ResponseBookingsDto> getBookings(@AuthenticationPrincipal Jwt jwt, @RequestParam Integer page){
        return ResponseEntity.status(201).body(bookingService.getBookings(jwt.getSubject(), page));
    }
}
