package com.project.roomly.repository;

import com.project.roomly.entity.HotelMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelMediaRepository extends JpaRepository<HotelMedia, Long> {

    @Query("select m.url from Hotel h join h.media m where h.id = :hotelId")
    List<String> findMediaKeyByHotelId(@Param("hotelId") Long hotelId);


}
