package com.onelity.bookme;

import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.service.BookingService;
import com.onelity.bookme.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.sql.Date;
import java.sql.Time;

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenValidBooking_whenCreateBooking_thenReturnOk() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
        BookingDTO newBookingDTO = (BookingDTO) response.getBody();
        Assert.isTrue(newBookingDTO.getClass().equals(bookingDTO.getClass()));
    }

    @Test
    public void givenNullBooking_whenCreateBooking_thenReturnBadRequest() {
        ResponseEntity<?> response = bookingService.createBookingInDatabase(null);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Booking cannot be null"));
    }

    @Test
    public void givenBookingWithNonexistentRoom_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name '" + bookingDTO.getRoom() +
                "' does not exist"));
    }

    @Test
    public void givenBookingWithLongTitle_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        createRoomInDatabase();
        bookingDTO.setTitle("Title xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Booking title cannot be more than 100 characters"));
    }

    @Test
    public void givenBookingWithStartDateAfterEndDate_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Date startDate = new Date(2003, 03, 02);
        bookingDTO.setStartDate(startDate);
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Booking start date cannot be after booking end date"));
    }

    @Test
    public void givenBookingWithStartTimeAfterEndTime_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Time startTime = new Time(11, 00, 00);
        bookingDTO.setStartTime(startTime);
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Booking start time must be before booking end time"));
    }

    @Test
    public void givenBookingWithNegativeParticipants_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setParticipants(-10);
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Participants cannot be less than 0"));
    }

    @Test
    public void givenBookingWithParticipantsExceedingCapacity_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setParticipants(200);
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Number of participants in booking exceeds meeting room capacity"));
    }

    @Test
    public void givenBookingWithInvalidRepeatPattern_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        bookingDTO.setRepeat_pattern("Twice a week");
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Repeat option must either be null, 'every day', or 'every same day of the week'"));
    }

    @Test
    public void givenBookingWithNullRepeatButMultipleDates_whenCreateBooking_thenReturnBadRequest() {
        BookingDTO bookingDTO = createExampleBookingDTO();
        Date endDate = new Date(2003, 03, 10);
        bookingDTO.setEndDate(endDate);
        createRoomInDatabase();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("If booking does not repeat, start date should be same as end date"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingsWithNoTimeConflictRepeatsDaily_whenCreateBooking_thenReturnCreated() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(11, 00, 00));
        newBookingDTO.setEndTime(new Time(12, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingsWithOverlappingTimesRepeatsDaily_whenCreateBooking_thenReturnConflict() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name " + newBookingDTO.getRoom() +
                " is already booked for the same time"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingWithOverlappingTimesRepeatsWeeklyConflictingDays_whenCreateBooking_thenReturnConflict() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createBookingDTORepeatsSaturdaysMarch();
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        System.out.println(response.getStatusCode());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name " + newBookingDTO.getRoom() +
                " is already booked for the same time"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingWithOverlappingTimesRepeatsWeeklyNotConflicting_whenCreateBooking_thenReturnCreated() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsSaturdaysMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsSaturdaysMarch();
        // set new booking DTO to repeat Sundays instead
        newBookingDTO.setStartDate(new Date(2003, 03, 02));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingsWithNonOverlappingTimesBothDaily_whenCreateBooking_thenReturnCreated() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(12, 00, 00));
        newBookingDTO.setEndTime(new Time(14, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CREATED));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenBookingsWithOverlappingTimesNullRepeatPatterns_whenCreateBooking_thenReturnConflict() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        BookingDTO newBookingDTO = createExampleBookingDTO();
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name " + newBookingDTO.getRoom() +
                " is already booked for the same time"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenNewBookingOverlappingOnlyWithPreviousSelf_whenUpdateBooking_thenReturnOk() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(14, 00, 00));
        newBookingDTO.setEndTime(new Time(16, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        BookingDTO createdBooking = (BookingDTO) response.getBody();
        Long id = createdBooking.getId();
        newBookingDTO.setStartTime(new Time(15, 00, 00));
        newBookingDTO.setEndTime(new Time(18, 00, 00));
        response = bookingService.updateBookingInDatabase(id, newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenNewBookingOverlappingWithExistingBookings_whenUpdateBooking_thenReturnConflict() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createBookingDTORepeatsDailyInMarch();
        BookingDTO newBookingDTO = createBookingDTORepeatsDailyInMarch();
        newBookingDTO.setStartTime(new Time(14, 00, 00));
        newBookingDTO.setEndTime(new Time(16, 00, 00));
        bookingService.createBookingInDatabase(bookingDTO);
        ResponseEntity<?> response = bookingService.createBookingInDatabase(newBookingDTO);
        BookingDTO createdBooking = (BookingDTO) response.getBody();
        Long id = createdBooking.getId();
        newBookingDTO.setStartTime(new Time(06, 00, 00));
        newBookingDTO.setEndTime(new Time(10, 00, 00));
        response = bookingService.updateBookingInDatabase(id, newBookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name " + bookingDTO.getRoom() +
                " is already booked for the same time"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenNewBookingWithNonexistentRoom_whenUpdateBooking_thenReturnBadRequest() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        BookingDTO createdBooking = (BookingDTO) response.getBody();
        Long id = createdBooking.getId();
        bookingDTO.setRoom("Room 2");
        response = bookingService.updateBookingInDatabase(id, bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Meeting room with name '" + bookingDTO.getRoom() +
                "' does not exist"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void givenNewBookingWithParticipantsAboveRoomLimit_whenUpdateBooking_thenReturnBadRequest() {
        createRoomInDatabase();
        BookingDTO bookingDTO = createExampleBookingDTO();
        ResponseEntity<?> response = bookingService.createBookingInDatabase(bookingDTO);
        BookingDTO createdBooking = (BookingDTO) response.getBody();
        Long id = createdBooking.getId();
        bookingDTO.setParticipants(200);
        response = bookingService.updateBookingInDatabase(id, bookingDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Number of participants in booking " +
                "exceeds meeting room capacity"));
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

    private void createRoomInDatabase() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(100);
        roomService.createRoomInDatabase(roomDTO);
    }
}
