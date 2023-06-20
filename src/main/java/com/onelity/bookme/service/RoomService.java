package com.onelity.bookme.service;

import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.onelity.bookme.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

public class RoomService {
// Business logic, validation and then do any needed CRUD through repository

    @Autowired
    private RoomRepository repo;

    @Autowired
    private ModelMapper modelMapper;

    public RoomService() {}

    public RoomDTO getRoomFromDatabase(Long id) {
        Room newRoom = repo.getReferenceById(id);
        return modelMapper.map(newRoom, RoomDTO.class);
    }

    public List<RoomDTO> getAllRoomsFromDatabase() {
        List<Room> allRooms = repo.findAll();
        List<RoomDTO> allRoomsDTO = new ArrayList<RoomDTO>();
        for (Room room : allRooms) {
            allRoomsDTO.add(modelMapper.map(room, RoomDTO.class));
        }
        return allRoomsDTO;
    }

    public RoomDTO createRoomInDatabase(RoomDTO roomDTO) {
        String location = roomDTO.getLocation();
        Room newRoom = repo.saveAndFlush(modelMapper.map(roomDTO, Room.class));
        return modelMapper.map(newRoom, RoomDTO.class);
    }

    public void deleteRoomInDatabase(Long id) {
        repo.deleteById(id);
    }

    public RoomDTO updateRoomInDatabase(Long id, RoomDTO roomDTO) {
        Room existingRoom = repo.getReferenceById(id);
        BeanUtils.copyProperties(modelMapper.map(roomDTO, Room.class), existingRoom, "room_id");
        return modelMapper.map(repo.saveAndFlush(existingRoom), RoomDTO.class);
    }

    public List<Room> getRoomsList() {
        return null;
    }



}
