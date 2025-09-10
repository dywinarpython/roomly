package com.project.roomly.repository;

import com.project.roomly.entity.HotelMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelMediaRepository extends JpaRepository<HotelMedia, Long> {

    @Query("select m.url from Hotel h join h.media m where h.id = :hotelId")
    List<String> findMediaKeyByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
            select m.url
            from HotelMedia m
            where m.hotel is null
            """)
    List<String> findMediaForDelete();

    @Modifying
    @Query("delete from HotelMedia m WHERE m.hotel is null")
    int deleteByHotelIsNull();


}
