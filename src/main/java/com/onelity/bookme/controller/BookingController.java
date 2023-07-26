package com.onelity.bookme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onelity.bookme.dto.BookingDTO;
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
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        return bookingService.getBookingFromDatabase(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllBookings() {
        return bookingService.getAllBookingsFromDatabase();
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createBooking(@RequestBody BookingDTO bookingDTO) {
        return bookingService.createBookingInDatabase(bookingDTO);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        return bookingService.deleteBookingInDatabase(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody BookingDTO bookingDTO) {
        return bookingService.updateBookingInDatabase(id, bookingDTO);
    }

    /**
     * Handles EntityNotFound exceptions when bookings with nonexistent ids are searched for
     *
     * @param exception
     *            the exception thrown when the repository attempts to get a nonpresent booking entity
     *
     * @return response entity with NotFound status and exception message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNoSuchElementFoundException(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
