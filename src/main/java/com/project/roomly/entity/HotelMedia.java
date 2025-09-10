package com.project.roomly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hotel_media")
@Getter
@Setter
public class HotelMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hotel_media_seq")
    @SequenceGenerator(allocationSize = 10, name = "hotel_media_seq", sequenceName = "hotel_media_seq")
    private Long id;

    public HotelMedia(String url, Hotel hotel) {
        this.url = url;
        this.hotel = hotel;
    }

    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
