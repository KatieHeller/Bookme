package com.onelity.bookme.controller;

import com.onelity.bookme.dto.RoomDTO;

import com.onelity.bookme.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meeting-rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        return roomService.getRoomFromDatabase(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        return roomService.getAllRoomsFromDatabase();
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createRoom(@RequestBody RoomDTO roomDTO) {
        return roomService.createRoomInDatabase(roomDTO);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoomInDatabase(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
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
    public ResponseEntity<String> handleNameAlreadyExistsException(
            PSQLException exception
    ) {
        if (exception.getMessage().contains("violates unique constraint")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(exception.getMessage());
        }
        else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }

    }

}
