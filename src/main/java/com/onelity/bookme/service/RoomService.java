package com.onelity.bookme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.onelity.bookme.ErrorResponse;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.RoomRepository;

/**
 * RoomService handles all business logic required for interacting with meeting rooms
 */
public class RoomService {

    @Autowired
    private RoomRepository repo;

    @Autowired
    private ModelMapper modelMapper;

    public RoomService() {
    }

    /**
     * Gets room from database with a specific id
     *
     * @param id
     *            id of requested room
     *
     * @return returns RoomDTO with OK status if room is present, or Error message with Not Found status if room is not
     *         present
     */
    public ResponseEntity<?> getRoomFromDatabase(Long id) {
        Optional<Room> room = repo.findById(id);
        if (room.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room with id " + id + " not found");
        }
        return new ResponseEntity<>(modelMapper.map(room, RoomDTO.class), HttpStatus.OK);
    }

    /**
     * Gets all rooms existing in database
     *
     * @return returns list of room DTOs with OK status
     */
    public ResponseEntity<?> getAllRoomsFromDatabase() {
        List<Room> allRooms = repo.findAll();
        List<RoomDTO> allRoomsDTO = new ArrayList<RoomDTO>();
        for (Room room : allRooms) {
            allRoomsDTO.add(modelMapper.map(room, RoomDTO.class));
        }
        return new ResponseEntity<>(allRoomsDTO, HttpStatus.OK);
    }

    /**
     * Performs necessary validation of inputted room object and creates room in database
     *
     * @param roomDTO
     *            roomDTO object that user would like to add to database
     *
     * @return returns same roomDTO object with its new id and Created status, or Bad Request if room is not valid
     */
    public ResponseEntity<?> createRoomInDatabase(RoomDTO roomDTO) {
        ResponseEntity<?> isValidRoom = checkForValidRoom(roomDTO);
        if (isValidRoom != null) {
            return isValidRoom;
        }
        Room newRoom = repo.saveAndFlush(modelMapper.map(roomDTO, Room.class));
        return new ResponseEntity<>(modelMapper.map(newRoom, RoomDTO.class), HttpStatus.CREATED);
    }

    /**
     * Deletes room in database
     *
     * @param id
     *            id of room user wants to delete
     *
     * @return returns ResponseEntity with No Content status
     */
    public ResponseEntity<?> deleteRoomInDatabase(Long id) {
        repo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Updates a room in database, checking that the new room information is all valid
     *
     * @param id
     *            id of room user wants to update
     * @param roomDTO
     *            roomDTO object with new room info
     *
     * @return returns updated roomDTO object with OK status if successful, or returns Bad Request status with error
     *         message if updated room would be invalid
     */
    public ResponseEntity<?> updateRoomInDatabase(Long id, RoomDTO roomDTO) {
        Room existingRoom = repo.getReferenceById(id);
        ResponseEntity<?> isValidRoom = checkForValidRoom(roomDTO);
        if (isValidRoom != null) {
            return isValidRoom;
        }
        Set<Booking> existingBookings = existingRoom.getBookings();
        for (Booking booking : existingBookings) {
            if (booking.getParticipants() > roomDTO.getCapacity()) {
                return badRequest("Room could not be updated because booking with title '" + booking.getTitle()
                        + "' has more participants (" + booking.getParticipants().toString() + ") than new capacity ("
                        + roomDTO.getCapacity().toString() + ")");
            }
        }
        BeanUtils.copyProperties(modelMapper.map(roomDTO, Room.class), existingRoom, "id");
        return new ResponseEntity<>(modelMapper.map(repo.saveAndFlush(existingRoom), RoomDTO.class), HttpStatus.OK);
    }

    /**
     * Performs validity checks on fields of a roomDTO
     *
     * @param roomDTO
     *            roomDTO object being checked
     *
     * @return returns null if room is valid, or returns Bad Request status with custom error message describing what
     *         field is invalid
     */
    private ResponseEntity<?> checkForValidRoom(RoomDTO roomDTO) {
        if (roomDTO == null) {
            return badRequest("Room can not be null");
        }
        if (roomDTO.getName() == null || roomDTO.getLocation() == null || roomDTO.getCapacity() == null) {
            return badRequest("Room fields can not be null");
        }
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

    /**
     * Handles creating Bad Request responses for given messages
     *
     * @param message
     *            String of why request is bad
     *
     * @return returns ResponseEntity object with custom message and Bad Request status
     */
    private ResponseEntity<?> badRequest(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
