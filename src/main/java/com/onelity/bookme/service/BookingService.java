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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookingService {

    @Autowired
    private BookingRepository repo;

    @Autowired RoomRepository roomRepo;

    public BookingService() {}

    public ResponseEntity<?> getBookingFromDatabase(Long id) {
        Booking booking = repo.getReferenceById(id);
        return new ResponseEntity<>(convertBookingToBookingDTO(booking), HttpStatus.OK);
    }

    public ResponseEntity<?> getAllBookingsFromDatabase() {
        List<Booking> allBookings = repo.findAll();
        List<BookingDTO> allBookingsDTO = new ArrayList<>();
        for (Booking booking : allBookings) {
            allBookingsDTO.add(convertBookingToBookingDTO(booking));
        }
        return new ResponseEntity<>(allBookingsDTO, HttpStatus.OK);
    }

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

    public ResponseEntity<?> deleteBookingInDatabase(Long id) {
        repo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> updateBookingInDatabase(Long id, BookingDTO bookingDTO) {
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

    private ResponseEntity<?> meetingRoomBooked(BookingDTO bookingDTO) {
        ErrorResponse errorResponse= new ErrorResponse();
        errorResponse.setMessage("Meeting room with name " + bookingDTO.getRoom() + " is already booked " +
                "for the same time");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

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

    private Booking convertBookingDTOToBooking(BookingDTO bookingDTO) {
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        return new Booking(bookingDTO.getId(), room, bookingDTO.getTitle(), bookingDTO.getDescription(),
                bookingDTO.getStartDate(), bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(),
                bookingDTO.getParticipants(), bookingDTO.getRepeat_pattern());
    }

    private ResponseEntity<?> checkForValidBooking(BookingDTO bookingDTO) {
        Room room = roomRepo.findByName(bookingDTO.getRoom());
        // Checks that meeting room exists
        if (room == null) {
            return badRequest("Meeting room with name '" + bookingDTO.getRoom() + "' does not exist");
        }
        // Checks that booking title is no more than 100 characters
        if (bookingDTO.getRoom().length() > 100) {
            return badRequest("Booking title cannot be more than 100 characters");
        }
        // Checks that booking start date is before end date
        if (bookingDTO.getStartDate().after(bookingDTO.getEndDate())) {
            return badRequest("Booking start date must be before booking end date");
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
