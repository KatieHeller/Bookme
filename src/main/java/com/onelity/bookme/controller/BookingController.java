package com.onelity.bookme.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.exception.BookingNotFoundException;
import com.onelity.bookme.exception.ConflictingBookingsException;
import com.onelity.bookme.exception.InvalidBookingException;
import com.onelity.bookme.exception.UnauthorizedUserException;
import com.onelity.bookme.service.BookingService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Controller for /bookings endpoint which directs all requests to methods implemented in bookingService module
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long id) throws Exception {
        return bookingService.getBookingFromDatabase(id);
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return bookingService.getAllBookingsFromDatabase();
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) throws Exception {
        return bookingService.createBookingInDatabase(bookingDTO);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id) throws Exception {
        bookingService.deleteBookingInDatabase(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<BookingDTO> update(@PathVariable Long id, @RequestBody BookingDTO bookingDTO)
            throws Exception {
        return bookingService.updateBookingInDatabase(id, bookingDTO);
    }

    /**
     * Handles BookingNotFound exceptions when bookings with nonexistent ids are searched for
     *
     * @param exception
     *            the exception thrown when the repository attempts to get a nonpresent booking entity
     *
     * @return response entity with NotFound status and exception message
     */
    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNoSuchElementFoundException(BookingNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleInvalidBookingException(InvalidBookingException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(ConflictingBookingsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleConflictingBookingsException(ConflictingBookingsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handleUnauthorizedUserException(UnauthorizedUserException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }
}
