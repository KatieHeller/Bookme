package com.onelity.bookme.controller;

import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Room;

import com.onelity.bookme.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meeting-rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping
    @RequestMapping("{id}")
    public RoomDTO getRoom(@PathVariable Long id) {
        return roomService.getRoomFromDatabase(id);
    }

    @GetMapping
    public List<RoomDTO> getAllRooms() {
        return roomService.getAllRoomsFromDatabase();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public RoomDTO createRoom(@RequestBody RoomDTO roomDTO) {
        return roomService.createRoomInDatabase(roomDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoomInDatabase(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public RoomDTO update(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        return roomService.updateRoomInDatabase(id, roomDTO);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNoSuchElementFoundException(
            EntityNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(PSQLException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleNameAlreadyExistsException(
            PSQLException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

}
