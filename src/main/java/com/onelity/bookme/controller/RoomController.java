package com.onelity.bookme.controller;

import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.service.RoomService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Controller for /meeting-rooms endpoint which directs all requests to methods implemented in roomService module. The
 * post, delete, and put mappings are secured such that only users with the ADMIN role can access them.
 */
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
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> createRoom(@RequestBody RoomDTO roomDTO) {
        return roomService.createRoomInDatabase(roomDTO);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoomInDatabase(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        return roomService.updateRoomInDatabase(id, roomDTO);
    }

    /**
     * Handles EntityNotFound exceptions when rooms with nonexistent ids are searched for
     *
     * @param exception
     *            the exception thrown when the repository attempts to get a nonpresent room entity
     *
     * @return response entity with NotFound status and exception message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNoSuchElementFoundException(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Handles PSQLExceptions
     *
     * @param exception
     *            exception that was thrown
     *
     * @return returns either a response entity with Conflict status, when a user attempts to create meeting rooms with
     *         the same name, or Bad Request status for all other cases
     */
    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<String> handleNameAlreadyExistsException(PSQLException exception) {
        if (exception.getMessage().contains("violates unique constraint")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }

    }

    /**
     * Converts 403 response codes to 401 for improper authentications
     *
     * @param exception
     *            Exception thrown when uses is not authenticated correctly
     *
     * @return Returns response entity with Unauthorized status and original exception message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleForbiddenException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }

}
