package com.project.roomly.repository;

import com.project.roomly.dto.Hotel.ResponseHotelDto;
import com.project.roomly.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Modifying
    @Query(value = """
            delete from hotel h
            where id = :hotelId and owner = :ownerId
            """, nativeQuery = true)
    int deleteByIdAndOwner(@Param("hotelId") Long roomId,
                           @Param("ownerId") UUID owner);

    boolean existsByIdAndOwner(Long id, UUID owner);




    @Query("select new com.project.roomly.dto.Hotel.ResponseHotelDto(h.name, h.address) from Hotel h where h.id = :hotelId")
    Optional<ResponseHotelDto> findHotel(@Param("hotelId") Long hotelId);




}
