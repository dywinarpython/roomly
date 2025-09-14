package com.project.roomly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "hotel")
@Getter
@Setter
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;


    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private UUID owner;

    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private Integer prepaymentPercentage;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "hotel")
    private List<Room> roomList = new ArrayList<>();


    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    private List<HotelMedia> media = new ArrayList<>();


}
