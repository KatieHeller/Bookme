package com.onelity.bookme.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onelity.bookme.dto.RoomDTO;
import jakarta.persistence.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Entity(name = "meeting_room")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long room_id;
    @Column(unique = true)
    private String name;
    private String location;
    private Integer capacity;

//    @Autowired
//    private ModelMapper modelMapper;

//    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    public Room() {
    }

//    public RoomDTO convertToDTO() {
//        RoomDTO roomDTO = modelMapper.map(this, RoomDTO.class);
//        return roomDTO;
//    }

    public Long getRoom_Id() {
        return room_id;
    }

    public void setRoom_id(Long room_id) {
        this.room_id = room_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }
}
