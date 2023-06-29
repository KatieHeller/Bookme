package com.onelity.bookme.service;

import com.onelity.bookme.ErrorResponse;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoomService {

    @Autowired
    private RoomRepository repo;

    @Autowired
    private ModelMapper modelMapper;

    public RoomService() {}

    public ResponseEntity<?> getRoomFromDatabase(Long id) {
        Room room = repo.getReferenceById(id);
        return new ResponseEntity<>(modelMapper.map(room, RoomDTO.class), HttpStatus.OK);
    }

    public ResponseEntity<?> getAllRoomsFromDatabase() {
        List<Room> allRooms = repo.findAll();
        List<RoomDTO> allRoomsDTO = new ArrayList<RoomDTO>();
        for (Room room : allRooms) {
            allRoomsDTO.add(modelMapper.map(room, RoomDTO.class));
        }
        return new ResponseEntity<>(allRoomsDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> createRoomInDatabase(RoomDTO roomDTO) {
        ResponseEntity<?> isValidRoom = checkForValidRoom(roomDTO);
        if (isValidRoom != null) {
            return isValidRoom;
        }
        Room newRoom = repo.saveAndFlush(modelMapper.map(roomDTO, Room.class));
        return new ResponseEntity<>(modelMapper.map(newRoom, RoomDTO.class), HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteRoomInDatabase(Long id) {
        repo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> updateRoomInDatabase(Long id, RoomDTO roomDTO) {
        Room existingRoom = repo.getReferenceById(id);
        ResponseEntity<?> isValidRoom = checkForValidRoom(roomDTO);
        if (isValidRoom != null) {
            return isValidRoom;
        }
        Set<Booking> existingBookings = existingRoom.getBookings();
        for (Booking booking : existingBookings) {
            if (booking.getParticipants() > roomDTO.getCapacity()) {
                return badRequest("Room could not be updated because booking with title '" + booking.getTitle() +
                        "' has more participants (" + booking.getParticipants().toString() + ") than new capacity (" +
                        roomDTO.getCapacity().toString() + ")");
            }
        }
        BeanUtils.copyProperties(modelMapper.map(roomDTO, Room.class), existingRoom, "id");
        return new ResponseEntity<>(modelMapper.map(repo.saveAndFlush(existingRoom), RoomDTO.class), HttpStatus.OK);
    }

    private ResponseEntity<?> checkForValidRoom(RoomDTO roomDTO) {
        if (roomDTO.getName().length() > 100) {
            return badRequest("Name cannot be more than 100 characters");
        }
        String location = roomDTO.getLocation();
        if (!location.equals("Thessaloniki") && !location.equals("Cologne")) {
            return badRequest("Location must be either 'Thessaloniki' or 'Cologne'");
        }
        if (roomDTO.getCapacity() < 0) {
            return badRequest("Capacity cannot be less than 0");
        }
        // return null only if room is valid
        return null;
    }

    private ResponseEntity<?> badRequest(String message) {
        ErrorResponse errorResponse= new ErrorResponse();
        errorResponse.setMessage(message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
