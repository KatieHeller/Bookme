package com.onelity.bookme.service;

import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.exception.InvalidRoomException;
import com.onelity.bookme.exception.RoomNotFoundException;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.RoomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
     * @return returns RoomDTO with OK status if room is present, or throws RoomNotFoundException
     */
    public ResponseEntity<RoomDTO> getRoomFromDatabase(Long id) throws Exception {
        Optional<Room> room = repo.findById(id);
        if (room.isEmpty()) {
            throw new RoomNotFoundException("Room with id " + id + " not found");
        }
        return new ResponseEntity<>(modelMapper.map(room, RoomDTO.class), HttpStatus.OK);
    }

    /**
     * Gets all rooms existing in database
     *
     * @return returns list of room DTOs with OK status
     */
    public ResponseEntity<List<RoomDTO>> getAllRoomsFromDatabase() {
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
     * @return returns same roomDTO object with its new id and Created status, or throws InvalidRoomException
     */
    public ResponseEntity<RoomDTO> createRoomInDatabase(RoomDTO roomDTO) throws Exception {
        checkForValidRoom(roomDTO);
        Room newRoom = repo.saveAndFlush(modelMapper.map(roomDTO, Room.class));
        return new ResponseEntity<>(modelMapper.map(newRoom, RoomDTO.class), HttpStatus.CREATED);
    }

    /**
     * Deletes room in database
     *
     * @param id
     *            id of room user wants to delete
     */
    public void deleteRoomInDatabase(Long id) {
        repo.deleteById(id);
    }

    /**
     * Updates a room in database, checking that the new room information is all valid
     *
     * @param id
     *            id of room user wants to update
     * @param roomDTO
     *            roomDTO object with new room info
     *
     * @return returns updated roomDTO object with OK status if successful, or throws InvalidRoomException
     */
    public ResponseEntity<RoomDTO> updateRoomInDatabase(Long id, RoomDTO roomDTO) throws Exception {
        Room existingRoom = repo.getReferenceById(id);
        checkForValidRoom(roomDTO);
        Set<Booking> existingBookings = existingRoom.getBookings();
        for (Booking booking : existingBookings) {
            if (booking.getParticipants() > roomDTO.getCapacity()) {
                throw new InvalidRoomException("Room could not be updated because booking with title '"
                        + booking.getTitle() + "' has more participants (" + booking.getParticipants().toString()
                        + ") than new capacity (" + roomDTO.getCapacity().toString() + ")");
            }
        }
        BeanUtils.copyProperties(modelMapper.map(roomDTO, Room.class), existingRoom, "id");
        return new ResponseEntity<>(modelMapper.map(repo.saveAndFlush(existingRoom), RoomDTO.class), HttpStatus.OK);
    }

    /**
     * Throws InvalidRoomException if any fields of roomDTO are not valid, otherwise does nothing
     *
     * @param roomDTO
     *            roomDTO object being checked
     */
    private void checkForValidRoom(RoomDTO roomDTO) throws Exception {
        if (roomDTO == null) {
            throw new InvalidRoomException("Room can not be null");
        }
        if (roomDTO.getName() == null || roomDTO.getLocation() == null || roomDTO.getCapacity() == null) {
            throw new InvalidRoomException("Room fields can not be null");
        }
        if (roomDTO.getName().length() > 100) {
            throw new InvalidRoomException("Name cannot be more than 100 characters");
        }
        String location = roomDTO.getLocation();
        if (!location.equals("Thessaloniki") && !location.equals("Cologne")) {
            throw new InvalidRoomException("Location must be either 'Thessaloniki' or 'Cologne'");
        }
        if (roomDTO.getCapacity() < 0) {
            throw new InvalidRoomException("Capacity cannot be less than 0");
        }
    }

}
