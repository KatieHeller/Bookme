package com.onelity.bookme.service;

import com.onelity.bookme.ErrorResponse;
import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * BookingService handles all business logic required for interacting with bookings
 */
public class BookingService {

    @Autowired
    private BookingRepository repo;

    @Autowired RoomRepository roomRepo;

    public BookingService() {}

    /**
     * Gets booking from database with a specific id
     * @param id id of requested booking
     * @return returns BookingDTO with OK status if booking is present, or Error message with Not Found status if
     * booking is not present
     */
    public ResponseEntity<?> getBookingFromDatabase(Long id) {
        Optional<Booking> booking = repo.findById(id);
        if (booking.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Booking with id " + id + " not found");
        }
        return new ResponseEntity<>(convertBookingToBookingDTO(booking.get()), HttpStatus.OK);
    }

    /**
     * Gets all bookings existing in database
     * @return returns list of booking DTOs with OK status
     */
    public ResponseEntity<?> getAllBookingsFromDatabase() {
        List<Booking> allBookings = repo.findAll();
        List<BookingDTO> allBookingsDTO = new ArrayList<>();
        for (Booking booking : allBookings) {
            allBookingsDTO.add(convertBookingToBookingDTO(booking));
        }
        return new ResponseEntity<>(allBookingsDTO, HttpStatus.OK);
    }

    /**
     * Performs necessary validation of inputted booking object and creates booking in database
     * @param bookingDTO bookingDTO object that user would like to add to database
     * @return returns same bookingDTO object with its new id and Created status, or Conflict or Bad Request if
     * booking is not valid
     */
    public ResponseEntity<?> createBookingInDatabase(BookingDTO bookingDTO) {
        ResponseEntity<?> isValidBooking = checkForValidBooking(bookingDTO);
        if (isValidBooking != null) {
            return isValidBooking;
        }
        ResponseEntity<?> hasConflictingBooking = checkForConflictingBookings(0L, bookingDTO, false);
        if (hasConflictingBooking != null) {
            return hasConflictingBooking;
        }
        Booking newBooking = repo.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        BookingDTO newBookingDTO = convertBookingToBookingDTO(newBooking);
        return new ResponseEntity<>(newBookingDTO, HttpStatus.CREATED);
    }

    /**
     * Deletes a booking in database, checking that the booking actually exists and the current user is authorized to
     * delete it
     * @param id id of booking that user wants to delete
     * @return returns No content if successful deletion or if booking was already not present, or returns Unauthorized
     * if user was not authorized to delete this
     */
    public ResponseEntity<?> deleteBookingInDatabase(Long id) {
        Optional<Booking> optionalBooking = repo.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            ResponseEntity<?> isAuthenticatedUser = checkIfAuthenticatedUser(booking);
            if (isAuthenticatedUser != null) {
                return isAuthenticatedUser;
            }
            repo.deleteById(id);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Updates a booking in database, checking that booking actually exists, user is authorized to update this, and the
     * new booking information is all valid
     * @param id id of booking user wants to update
     * @param bookingDTO information that user wants to update the booking with
     * @return Returns new bookingDTO object and Ok status if successful update, Unauthorized if user not authorized,
     * Bad Request if booking is invalid, or Conflict if booking times overlap with another booking in same room
     */
    public ResponseEntity<?> updateBookingInDatabase(Long id, BookingDTO bookingDTO) {
        Optional<Booking> optionalBooking = repo.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            ResponseEntity<?> isAuthenticatedUser = checkIfAuthenticatedUser(booking);
            if (isAuthenticatedUser != null) {
                return isAuthenticatedUser;
            }
        }
        // If existingBooking not found, exception is handled in BookingController
        Booking existingBooking = repo.getReferenceById(id);
        ResponseEntity<?> isValidBooking = checkForValidBooking(bookingDTO);
        if (isValidBooking != null) {
            return isValidBooking;
        }
        // If any dates or times of booking have been changed, do check for conflicting bookings
        if (!existingBooking.getStartDate().toLocalDate().equals(bookingDTO.getStartDate().toLocalDate()) ||
        !existingBooking.getEndDate().toLocalDate().equals(bookingDTO.getEndDate().toLocalDate()) ||
        !existingBooking.getStartTime().equals(bookingDTO.getStartTime()) ||
        !existingBooking.getEndTime().equals(bookingDTO.getEndTime())) {
            ResponseEntity<?> hasConflictingBooking = checkForConflictingBookings(id, bookingDTO, true);
            if (hasConflictingBooking != null) {
                return hasConflictingBooking;
            }
        }
        BeanUtils.copyProperties(convertBookingDTOToBooking(bookingDTO), existingBooking, "id");
        return new ResponseEntity<>(convertBookingToBookingDTO(repo.saveAndFlush(existingBooking)), HttpStatus.OK);
    }

    /**
     * Creates ResponseEntity for case when bookings conflict
     * @param bookingDTO booking which is already booked at time
     * @return returns ResponseEntity object with Conflict status and error message containing booked room name
     */
    private ResponseEntity<?> meetingRoomBooked(BookingDTO bookingDTO) {
        ErrorResponse errorResponse= new ErrorResponse();
        errorResponse.setMessage("Meeting room with name " + bookingDTO.getRoom() + " is already booked " +
                "for the same time");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Creates ResponseEntity for any case where user input is invalid
     * @param message message that should be displayed to user
     * @return returns ResponseEntity object with Bad Request status and error message
     */
    private ResponseEntity<?> badRequest(String message) {
        ErrorResponse errorResponse= new ErrorResponse();
        errorResponse.setMessage(message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private BookingDTO convertBookingToBookingDTO(Booking booking) {
        return new BookingDTO(booking.getId(), booking.getRoom().getName(),
                booking.getTitle(), booking.getDescription(), booking.getStartDate(), booking.getEndDate(),
                booking.getStartTime(), booking.getEndTime(), booking.getParticipants(), booking.getRepeat_pattern());
    }

    /**
     * Converts a BookingDTO to a Booking object, taking into account the current authenticated user so the booking
     * creator can be saved
     * @param bookingDTO bookingDTO being converted
     * @return returns equivalent Booking object
     */
    private Booking convertBookingDTOToBooking(BookingDTO bookingDTO) {
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String booking_creator = user.getUsername();
        return new Booking(bookingDTO.getId(), room, bookingDTO.getTitle(), bookingDTO.getDescription(),
                bookingDTO.getStartDate(), bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(),
                bookingDTO.getParticipants(), bookingDTO.getRepeat_pattern(), booking_creator);
    }

    /**
     * Checks if current autheticated user either has Admin role or is the creator of a booking
     * @param booking Booking a user is trying to update or delete
     * @return returns null if user is authorized or a Response Entity with Unauthorized status otherwise
     */
    private ResponseEntity<?> checkIfAuthenticatedUser(Booking booking) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) || user.getUsername().equals(booking.getCreator_username())) {
            return null;
        }
        ErrorResponse errorResponse= new ErrorResponse();
        errorResponse.setMessage("Access denied");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Performs all validity checks on fields of a bookingDTO
     * @param bookingDTO bookingDTO object that needs to be verified
     * @return returns null if booking is valid or ResponseEntity with Bad Request status and custom error message
     * informing user which field is invalid
     */
    private ResponseEntity<?> checkForValidBooking(BookingDTO bookingDTO) {
        if (bookingDTO == null) {
            return badRequest("Booking cannot be null");
        }
        if (bookingDTO.getRoom() == null) {
            return badRequest("Room can not be null");
        }
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        // Checks that meeting room exists
        if (room == null) {
            return badRequest("Meeting room with name '" + bookingDTO.getRoom() + "' does not exist");
        }
        if (bookingDTO.getTitle() == null || bookingDTO.getStartDate() == null ||
                bookingDTO.getEndDate() == null || bookingDTO.getStartTime() == null ||
                bookingDTO.getEndTime() == null || bookingDTO.getParticipants() == null) {
            return badRequest("Fields of booking cannot be null (except description or repeat option)");
        }
        // Checks that booking title is no more than 100 characters
        if (bookingDTO.getTitle().length() > 100) {
            return badRequest("Booking title cannot be more than 100 characters");
        }
        // Checks that booking start date is not after end date
        if (bookingDTO.getStartDate().after(bookingDTO.getEndDate())) {
            return badRequest("Booking start date cannot be after booking end date");
        }
        // Checks that booking start time is before end time
        if (bookingDTO.getStartTime().after(bookingDTO.getEndTime())) {
            return badRequest("Booking start time must be before booking end time");
        }
        if (bookingDTO.getParticipants() < 0) {
            return badRequest("Participants cannot be less than 0");
        }
        // Checks that participants size does not exceed meeting room capacity
        if (bookingDTO.getParticipants() > room.getCapacity()) {
            return badRequest("Number of participants in booking exceeds meeting room capacity");
        }
        // Checks that repeat option is either null, 'every day', or 'every same day of the week'
        String repeatPattern = bookingDTO.getRepeat_pattern();
        if (repeatPattern != null && !repeatPattern.equals("every day") &&
                !repeatPattern.equals("every same day of the week")) {
            return badRequest("Repeat option must either be null, 'every day', or 'every same day of the week'");
        }
        // Checks that if repeat option is null, start date is same as end date
        if (repeatPattern == null &&
                !bookingDTO.getStartDate().toLocalDate().isEqual(bookingDTO.getEndDate().toLocalDate())) {
            return badRequest("If booking does not repeat, start date should be same as end date");
        }
        // Returns null only if this booking is valid
        return null;
    }

    // When isUpdate is true, the id will be used so that if the new booking times overlap with itself, won't be counted

    /**
     * Verifies if a given booking will overlap times and room with any existing bookings in database
     * @param id used in case of PUT method for bookings, the id of booking being updated
     * @param bookingDTO bookingDTO that is being checked for conflicts
     * @param isUpdate when true, the id will be used so that if a booking's new times overlap with its previous times,
     *                 this conflict will be ignored
     * @return returns null if there are no conflicts or a ResponseEntity with Conflict status and error message
     */
    private ResponseEntity<?> checkForConflictingBookings(Long id, BookingDTO bookingDTO, boolean isUpdate) {
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        String repeatPattern = bookingDTO.getRepeat_pattern();
        // Gets all bookings with same room with overlapping dates and times
        // These are potential conflicts, depending on their repeat patterns
        List<Booking> overlappingBookings;
        if (isUpdate) {
            overlappingBookings = repo.findOverlappingBookingsUpdate(bookingDTO.getStartDate(),
                    bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(), room, id);
        }
        else {
            overlappingBookings = repo.findOverlappingBookingsCreate(bookingDTO.getStartDate(),
                    bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(), room);
        }
        Calendar c = Calendar.getInstance();
        c.setTime(bookingDTO.getStartDate());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        for (Booking booking : overlappingBookings) {
            // All bookings in overlappingBookings have time conflicts, so must only check for date conflicts
            String otherRepeatPattern = booking.getRepeat_pattern();
            // If either repeat pattern is 'every day', there will be conflict
            if (repeatPattern != null && repeatPattern.equals("every day")) {
                return meetingRoomBooked(bookingDTO);
            }
            if (otherRepeatPattern != null && otherRepeatPattern.equals("every day")) {
                return meetingRoomBooked(bookingDTO);
            }
            // If either repeat pattern is 'every same day of the week', conflict if start dates are same day of week
            c.setTime(booking.getStartDate());
            int otherDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            if (otherRepeatPattern != null || repeatPattern != null) {
                if (dayOfWeek == otherDayOfWeek) {
                    return meetingRoomBooked(bookingDTO);
                }
            }
            // This else only occurs when both repeat patterns are null, meaning there must be overlap
            else {
                return meetingRoomBooked(bookingDTO);
            }
        }
        // Returns null if there are no conflicting bookings
        return null;
    }

}
