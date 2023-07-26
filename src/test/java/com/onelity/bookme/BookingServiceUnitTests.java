package com.onelity.bookme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;
import java.sql.Time;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.exception.ConflictingBookingsException;
import com.onelity.bookme.exception.InvalidBookingException;
import com.onelity.bookme.exception.InvalidRoomException;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.service.BookingService;
import com.onelity.bookme.service.RoomService;

@SpringBootTest
public class BookingServiceUnitTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @BeforeTestClass
    public void classSetup() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @AfterEach
    public void teardown() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidBooking_whenCreateBooking_thenReturnOk() throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        createRoomInDatabase();
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
        BookingDTO newBookingDTO = response.getBody();
        Assert.isTrue(newBookingDTO.getClass().equals(bookingDTO.getClass()));
    }

    @Test
    public void givenNullBooking_whenCreateBooking_thenThrowInvalidBookingException() throws Exception {
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(null),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Booking cannot be null", thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewBookingOverlappingOnlyWithPreviousSelf_whenUpdateBooking_thenReturnOk() throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(14, 00, 00));
        newBookingDTO.setEndTime(new Time(16, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(newBookingDTO);
        BookingDTO createdBooking = response.getBody();
        Long id = createdBooking.getId();
        newBookingDTO.setStartTime(new Time(15, 00, 00));
        newBookingDTO.setEndTime(new Time(18, 00, 00));
        response = bookingService.updateBookingInDatabase(id, newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewBookingOverlappingWithExistingBookings_whenUpdateBooking_thenThrowConflictingBookingsException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(14, 00, 00));
        newBookingDTO.setEndTime(new Time(16, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(newBookingDTO);
        BookingDTO createdBooking = response.getBody();
        Long id = createdBooking.getId();
        newBookingDTO.setStartTime(new Time(06, 00, 00));
        newBookingDTO.setEndTime(new Time(10, 00, 00));
        ConflictingBookingsException thrown = assertThrows(ConflictingBookingsException.class,
                () -> bookingService.updateBookingInDatabase(id, newBookingDTO),
                "Expected updateBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name " + bookingDTO.getRoom() + " is already booked for the same time",
                thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewBookingWithNonexistentRoom_whenUpdateBooking_thenThrowInvalidBookingException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(bookingDTO);
        BookingDTO createdBooking = response.getBody();
        Long id = createdBooking.getId();
        bookingDTO.setRoom("Room 2");
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.updateBookingInDatabase(id, bookingDTO),
                "Expected updateBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name '" + bookingDTO.getRoom() + "' does not exist", thrown.getMessage());
    }

    @Test
    public void givenBookingWithNonexistentRoom_whenCreateBooking_thenThrowInvalidBookingException() throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name '" + bookingDTO.getRoom() + "' does not exist", thrown.getMessage());
    }

    @Test
    public void givenBookingWithLongTitle_whenCreateBooking_thenThrowInvalidBookingException() throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        createRoomInDatabase();
        bookingDTO.setTitle("Title xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Booking title cannot be more than 100 characters", thrown.getMessage());
    }

    @Test
    public void givenBookingWithStartDateAfterEndDate_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Date startDate = new Date(2003, 03, 02);
        bookingDTO.setStartDate(startDate);
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Booking start date cannot be after booking end date", thrown.getMessage());
    }

    @Test
    public void givenBookingWithStartTimeAfterEndTime_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Time startTime = new Time(11, 00, 00);
        bookingDTO.setStartTime(startTime);
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Booking start time must be before booking end time", thrown.getMessage());
    }

    @Test
    public void givenBookingWithInvalidRepeatPattern_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setRepeat_pattern("Twice a week");
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Repeat option must either be null, 'every day', or 'every same day of the week'",
                thrown.getMessage());
    }

    @Test
    public void givenBookingWithNegativeParticipants_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setParticipants(-10);
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Participants cannot be less than 0", thrown.getMessage());
    }

    @Test
    public void givenBookingWithParticipantsExceedingCapacity_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setParticipants(200);
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Number of participants in booking exceeds meeting room capacity", thrown.getMessage());
    }

    @Test
    public void givenBookingWithNullRepeatButMultipleDates_whenCreateBooking_thenThrowInvalidBookingException()
            throws Exception {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Date endDate = new Date(2003, 03, 10);
        bookingDTO.setEndDate(endDate);
        createRoomInDatabase();
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.createBookingInDatabase(bookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("If booking does not repeat, start date should be same as end date", thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingsWithNoTimeConflictRepeatsDaily_whenCreateBooking_thenReturnCreated() throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(11, 00, 00));
        newBookingDTO.setEndTime(new Time(12, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingsWithOverlappingTimesRepeatsDaily_whenCreateBooking_thenThrowConflictingBookingsException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        bookingService.createBookingInDatabase(bookingDTO);
        ConflictingBookingsException thrown = assertThrows(ConflictingBookingsException.class,
                () -> bookingService.createBookingInDatabase(newBookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name " + newBookingDTO.getRoom() + " is already booked for the same time",
                thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingWithOverlappingTimesRepeatsWeeklyConflictingDays_whenCreateBooking_thenThrowConflictingBookingsException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsSaturdaysMarch();
        bookingService.createBookingInDatabase(bookingDTO);
        ConflictingBookingsException thrown = assertThrows(ConflictingBookingsException.class,
                () -> bookingService.createBookingInDatabase(newBookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name " + newBookingDTO.getRoom() + " is already booked for the same time",
                thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingWithOverlappingTimesRepeatsWeeklyNotConflicting_whenCreateBooking_thenReturnCreated()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsSaturdaysMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsSaturdaysMarch();
        // set new booking DTO to repeat Sundays instead
        newBookingDTO.setStartDate(new Date(2003, 03, 02));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingsWithNonOverlappingTimesBothDaily_whenCreateBooking_thenReturnCreated() throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(12, 00, 00));
        newBookingDTO.setEndTime(new Time(14, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingsWithOverlappingTimesNullRepeatPatterns_whenCreateBooking_thenThrowConflictingBookingsException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createExampleBookingDTO();
        bookingService.createBookingInDatabase(bookingDTO);
        ConflictingBookingsException thrown = assertThrows(ConflictingBookingsException.class,
                () -> bookingService.createBookingInDatabase(newBookingDTO),
                "Expected createBookingInDatabase() to throw, but it didn't");
        assertEquals("Meeting room with name " + newBookingDTO.getRoom() + " is already booked for the same time",
                thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewBookingWithParticipantsAboveRoomLimit_whenUpdateBooking_thenReturnInvalidBookingException()
            throws Exception {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        ResponseEntity<BookingDTO> response = bookingService.createBookingInDatabase(bookingDTO);
        BookingDTO createdBooking = response.getBody();
        Long id = createdBooking.getId();
        bookingDTO.setParticipants(200);
        InvalidBookingException thrown = assertThrows(InvalidBookingException.class,
                () -> bookingService.updateBookingInDatabase(id, bookingDTO),
                "Expected updateBookingInDatabase() to throw, but it didn't");
        assertEquals("Number of participants in booking " + "exceeds meeting room capacity", thrown.getMessage());
    }

    private BookingDTO createExampleBookingDTO() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setTitle("Booking 1");
        bookingDTO.setRoom("Room 1");
        bookingDTO.setDescription("Description 1");
        Date date = new Date(2003, 03, 01);
        bookingDTO.setStartDate(date);
        bookingDTO.setEndDate(date);
        Time startTime = new Time(07, 00, 00);
        Time endTime = new Time(10, 00, 00);
        bookingDTO.setStartTime(startTime);
        bookingDTO.setEndTime(endTime);
        bookingDTO.setParticipants(100);
        return bookingDTO;
    }

    private BookingDTO createBookingDTORepeatsDailyInMarch() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setTitle("Booking 1");
        bookingDTO.setRoom("Room 1");
        bookingDTO.setDescription("Description 1");
        bookingDTO.setStartDate(new Date(2003, 03, 01));
        bookingDTO.setEndDate(new Date(2003, 03, 31));
        Time startTime = new Time(07, 00, 00);
        Time endTime = new Time(10, 00, 00);
        bookingDTO.setStartTime(startTime);
        bookingDTO.setEndTime(endTime);
        bookingDTO.setParticipants(100);
        bookingDTO.setRepeat_pattern("every day");
        return bookingDTO;
    }

    private BookingDTO createBookingDTORepeatsSaturdaysMarch() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setTitle("Booking 1");
        bookingDTO.setRoom("Room 1");
        bookingDTO.setDescription("Description 1");
        bookingDTO.setStartDate(new Date(2003, 03, 01));
        bookingDTO.setEndDate(new Date(2003, 03, 31));
        Time startTime = new Time(07, 00, 00);
        Time endTime = new Time(10, 00, 00);
        bookingDTO.setStartTime(startTime);
        bookingDTO.setEndTime(endTime);
        bookingDTO.setParticipants(100);
        bookingDTO.setRepeat_pattern("every same day of the week");
        return bookingDTO;
    }

    private void createRoomInDatabase() throws Exception {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(100);
        roomService.createRoomInDatabase(roomDTO);
    }
}
