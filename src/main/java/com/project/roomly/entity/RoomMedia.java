package com.project.roomly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_media")
@Getter
@Setter
@NoArgsConstructor
public class RoomMedia {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_media_seq")
    @SequenceGenerator(allocationSize = 10, name = "room_media_seq", sequenceName = "room_media_seq")
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    public RoomMedia(String url, Room room) {
        this.url = url;
        this.room = room;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
