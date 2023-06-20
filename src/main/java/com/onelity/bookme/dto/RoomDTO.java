package com.onelity.bookme.dto;


import com.onelity.bookme.model.Room;
import com.onelity.bookme.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class RoomDTO {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;

    @Autowired
    private RoomService roomService;

//    @Autowired
//    private ModelMapper modelMapper;


//    public Room convertToEntity() {
//        return modelMapper.map(this, Room.class);
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

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
}
