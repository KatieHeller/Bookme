package com.onelity.bookme.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.onelity.bookme.ErrorResponse;
import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.exception.BookingNotFoundException;
import com.onelity.bookme.exception.ConflictingBookingsException;
import com.onelity.bookme.exception.InvalidBookingException;
import com.onelity.bookme.exception.InvalidRoomException;
import com.onelity.bookme.exception.UnauthorizedUserException;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;

/**
 * BookingService handles all business logic required for interacting with bookings
 */
public class BookingService {

    @Autowired
    private BookingRepository repo;

    @Autowired
    RoomRepository roomRepo;

    public BookingService() {
    }

    /**
     * Gets booking from database with a specific id
     *
     * @param id
     *            id of requested booking
     *
     * @return returns BookingDTO with OK status if booking is present, or throws BookingNotFoundException
     */
    public ResponseEntity<BookingDTO> getBookingFromDatabase(Long id) throws Exception {
        Optional<Booking> booking = repo.findById(id);
        if (booking.isEmpty()) {
            throw new BookingNotFoundException("Booking with id " + id + " not found");
        }
        return new ResponseEntity<>(convertBookingToBookingDTO(booking.get()), HttpStatus.OK);
    }

    /**
     * Gets all bookings existing in database
     *
     * @return returns list of booking DTOs with OK status
     */
    public ResponseEntity<List<BookingDTO>> getAllBookingsFromDatabase() {
        List<Booking> allBookings = repo.findAll();
        List<BookingDTO> allBookingsDTO = new ArrayList<>();
        for (Booking booking : allBookings) {
            allBookingsDTO.add(convertBookingToBookingDTO(booking));
        }
        return new ResponseEntity<>(allBookingsDTO, HttpStatus.OK);
    }

    /**
     * Performs necessary validation of inputted booking object and creates booking in database
     *
     * @param bookingDTO
     *            bookingDTO object that user would like to add to database
     *
     * @return returns same bookingDTO object with its new id and Created status, or throws InvalidBookingException or
     *         ConflictingBookingException
     */
    public ResponseEntity<BookingDTO> createBookingInDatabase(BookingDTO bookingDTO) throws Exception {
        checkForValidBooking(bookingDTO);
        checkForConflictingBookings(0L, bookingDTO, false);
        Booking newBooking = repo.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        BookingDTO newBookingDTO = convertBookingToBookingDTO(newBooking);
        return new ResponseEntity<>(newBookingDTO, HttpStatus.CREATED);
    }

    /**
     * Deletes a booking in database, or throws UnauthorizedUserException
     *
     * @param id
     *            id of booking that user wants to delete
     */
    public void deleteBookingInDatabase(Long id) throws Exception {
        Optional<Booking> optionalBooking = repo.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            checkIfAuthenticatedUser(booking);
            repo.deleteById(id);
        }
    }

    /**
     * Updates a booking in database, checking that booking actually exists, user is authorized to update this, and the
     * new booking information is all valid
     *
     * @param id
     *            id of booking user wants to update
     * @param bookingDTO
     *            information that user wants to update the booking with
     *
     * @return Returns new bookingDTO object and Ok status if successful update, or throws UnauthorizedUserException or
     *         InvalidBookingException or ConflictingBookingsException
     */
    public ResponseEntity<BookingDTO> updateBookingInDatabase(Long id, BookingDTO bookingDTO) throws Exception {
        Optional<Booking> optionalBooking = repo.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            checkIfAuthenticatedUser(booking);
        }
        // If existingBooking not found, exception is handled in BookingController
        Booking existingBooking = repo.getReferenceById(id);
        checkForValidBooking(bookingDTO);
        // If any dates or times of booking have been changed, do check for conflicting bookings
        if (!existingBooking.getStartDate().toLocalDate().equals(bookingDTO.getStartDate().toLocalDate())
                || !existingBooking.getEndDate().toLocalDate().equals(bookingDTO.getEndDate().toLocalDate())
                || !existingBooking.getStartTime().equals(bookingDTO.getStartTime())
                || !existingBooking.getEndTime().equals(bookingDTO.getEndTime())) {
            checkForConflictingBookings(id, bookingDTO, true);
        }
        BeanUtils.copyProperties(convertBookingDTOToBooking(bookingDTO), existingBooking, "id");
        return new ResponseEntity<>(convertBookingToBookingDTO(repo.saveAndFlush(existingBooking)), HttpStatus.OK);
    }

    private BookingDTO convertBookingToBookingDTO(Booking booking) {
        return new BookingDTO(booking.getId(), booking.getRoom().getName(), booking.getTitle(),
                booking.getDescription(), booking.getStartDate(), booking.getEndDate(), booking.getStartTime(),
                booking.getEndTime(), booking.getParticipants(), booking.getRepeat_pattern());
    }

    /**
     * Converts a BookingDTO to a Booking object, taking into account the current authenticated user so the booking
     * creator can be saved
     *
     * @param bookingDTO
     *            bookingDTO being converted
     *
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
     * Throws UnauthorizedUserException if current authenticated user is not authorized
     *
     * @param booking
     *            Booking a user is trying to update or delete
     */
    private void checkIfAuthenticatedUser(Booking booking) throws Exception {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !user.getUsername().equals(booking.getCreator_username())) {
            throw new UnauthorizedUserException("Access denied");
        }
    }

    /**
     * Throws InvalidBookingException if any part of bookingDTO is not valid
     *
     * @param bookingDTO
     *            bookingDTO object that needs to be verified
     */
    private void checkForValidBooking(BookingDTO bookingDTO) throws Exception {
        if (bookingDTO == null) {
            throw new InvalidBookingException("Booking cannot be null");
        }
        if (bookingDTO.getRoom() == null) {
            throw new InvalidBookingException("Room can not be null");
        }
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        // Checks that meeting room exists
        if (room == null) {
            throw new InvalidBookingException("Meeting room with name '" + bookingDTO.getRoom() + "' does not exist");
        }
        if (bookingDTO.getTitle() == null || bookingDTO.getStartDate() == null || bookingDTO.getEndDate() == null
                || bookingDTO.getStartTime() == null || bookingDTO.getEndTime() == null
                || bookingDTO.getParticipants() == null) {
            throw new InvalidBookingException("Fields of booking cannot be null (except description or repeat option)");
        }
        // Checks that booking title is no more than 100 characters
        if (bookingDTO.getTitle().length() > 100) {
            throw new InvalidBookingException("Booking title cannot be more than 100 characters");
        }
        // Checks that booking start date is not after end date
        if (bookingDTO.getStartDate().after(bookingDTO.getEndDate())) {
            throw new InvalidBookingException("Booking start date cannot be after booking end date");
        }
        // Checks that booking start time is before end time
        if (bookingDTO.getStartTime().after(bookingDTO.getEndTime())) {
            throw new InvalidBookingException("Booking start time must be before booking end time");
        }
        if (bookingDTO.getParticipants() < 0) {
            throw new InvalidBookingException("Participants cannot be less than 0");
        }
        // Checks that participants size does not exceed meeting room capacity
        if (bookingDTO.getParticipants() > room.getCapacity()) {
            throw new InvalidBookingException("Number of participants in booking exceeds meeting room capacity");
        }
        // Checks that repeat option is either null, 'every day', or 'every same day of the week'
        String repeatPattern = bookingDTO.getRepeat_pattern();
        if (repeatPattern != null && !repeatPattern.equals("every day")
                && !repeatPattern.equals("every same day of the week")) {
            throw new InvalidBookingException(
                    "Repeat option must either be null, 'every day', or 'every same day of the week'");
        }
        // Checks that if repeat option is null, start date is same as end date
        if (repeatPattern == null
                && !bookingDTO.getStartDate().toLocalDate().isEqual(bookingDTO.getEndDate().toLocalDate())) {
            throw new InvalidBookingException("If booking does not repeat, start date should be same as end date");
        }
    }

    // When isUpdate is true, the id will be used so that if the new booking times overlap with itself, won't be counted

    /**
     * Throws ConflictingBookingsException if booking will overlap times and room with any existing bookings in database
     *
     * @param id
     *            used in case of PUT method for bookings, the id of booking being updated
     * @param bookingDTO
     *            bookingDTO that is being checked for conflicts
     * @param isUpdate
     *            when true, the id will be used so that if a booking's new times overlap with its previous times, this
     *            conflict will be ignored
     */
    private void checkForConflictingBookings(Long id, BookingDTO bookingDTO, boolean isUpdate) throws Exception {
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        String repeatPattern = bookingDTO.getRepeat_pattern();
        // Gets all bookings with same room with overlapping dates and times
        // These are potential conflicts, depending on their repeat patterns
        List<Booking> overlappingBookings;
        if (isUpdate) {
            overlappingBookings = repo.findOverlappingBookingsUpdate(bookingDTO.getStartDate(), bookingDTO.getEndDate(),
                    bookingDTO.getStartTime(), bookingDTO.getEndTime(), room, id);
        } else {
            overlappingBookings = repo.findOverlappingBookingsCreate(bookingDTO.getStartDate(), bookingDTO.getEndDate(),
                    bookingDTO.getStartTime(), bookingDTO.getEndTime(), room);
        }
        Calendar c = Calendar.getInstance();
        c.setTime(bookingDTO.getStartDate());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        for (Booking booking : overlappingBookings) {
            // All bookings in overlappingBookings have time conflicts, so must only check for date conflicts
            String otherRepeatPattern = booking.getRepeat_pattern();
            // If either repeat pattern is 'every day', there will be conflict
            if (repeatPattern != null && repeatPattern.equals("every day")) {
                throw new ConflictingBookingsException(
                        "Meeting room with name " + bookingDTO.getRoom() + " is already booked " + "for the same time");
            }
            if (otherRepeatPattern != null && otherRepeatPattern.equals("every day")) {
                throw new ConflictingBookingsException(
                        "Meeting room with name " + bookingDTO.getRoom() + " is already booked " + "for the same time");
            }
            // If either repeat pattern is 'every same day of the week', conflict if start dates are same day of week
            c.setTime(booking.getStartDate());
            int otherDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            if (otherRepeatPattern != null || repeatPattern != null) {
                if (dayOfWeek == otherDayOfWeek) {
                    throw new ConflictingBookingsException("Meeting room with name " + bookingDTO.getRoom()
                            + " is already booked " + "for the same time");
                }
            }
            // This else only occurs when both repeat patterns are null, meaning there must be overlap
            else {
                throw new ConflictingBookingsException(
                        "Meeting room with name " + bookingDTO.getRoom() + " is already booked " + "for the same time");
            }
        }
    }

}
