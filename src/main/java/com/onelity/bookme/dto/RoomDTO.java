package com.onelity.bookme.dto;

import org.springframework.beans.factory.annotation.Autowired;

import com.onelity.bookme.service.RoomService;

/**
 * DTO class for room entities
 */
public class RoomDTO {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;

    @Autowired
    private RoomService roomService;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
