package com.project.roomly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "count_room", nullable = false)
    private Integer countRoom;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "price_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceDay;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room")
    private List<RoomMedia> media = new ArrayList<>();
}