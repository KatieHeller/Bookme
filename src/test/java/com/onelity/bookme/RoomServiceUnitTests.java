package com.onelity.bookme;

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
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.service.BookingService;
import com.onelity.bookme.service.RoomService;

@SpringBootTest
public class RoomServiceUnitTests {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeTestClass
    public void classSetup() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @AfterEach
    public void teardown() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    public void givenLongName_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setName("Room 1 xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Name cannot be more than 100 characters"));
    }

    @Test
    public void givenInvalidLocation_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setLocation("Greece");
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Location must be either 'Thessaloniki' or 'Cologne'"));
    }

    @Test
    public void givenNegativeCapacity_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setCapacity(-10);
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Capacity cannot be less than 0"));
    }

    @Test
    public void givenNullRoom_whenCreateRoom_thenReturnBadRequest() {
        ResponseEntity<?> response = roomService.createRoomInDatabase(null);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Room can not be null"));
    }

    @Test
    public void givenNullRoomName_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setName(null);
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Room fields can not be null"));
    }

    @Test
    public void givenNullLocation_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setLocation(null);
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Room fields can not be null"));
    }

    @Test
    public void givenNullCapacity_whenCreateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setCapacity(null);
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage().equals("Room fields can not be null"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewRoomCapacityBelowBookingParticipants_whenUpdateRoom_thenReturnBadRequest() {
        RoomDTO roomDTO = createExampleRoomDTO();
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setTitle("Booking 1");
        bookingDTO.setDescription("Description");
        bookingDTO.setRoom("Room 1");
        bookingDTO.setStartDate(new Date(2003, 03, 01));
        bookingDTO.setEndDate(new Date(2003, 03, 01));
        bookingDTO.setStartTime(new Time(10, 00, 00));
        bookingDTO.setEndTime(new Time(12, 00, 00));
        bookingDTO.setParticipants(10);
        ResponseEntity<?> response = roomService.createRoomInDatabase(roomDTO);
        RoomDTO addedRoom = (RoomDTO) response.getBody();
        Long id = addedRoom.getId();
        bookingService.createBookingInDatabase(bookingDTO);
        roomDTO.setCapacity(5);
        response = roomService.updateRoomInDatabase(id, roomDTO);
        Assert.isTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.isTrue(errorResponse.getMessage()
                .equals("Room could not be updated because booking with " + "title '" + bookingDTO.getTitle()
                        + "' has more participants (" + bookingDTO.getParticipants().toString()
                        + ") than new capacity (" + roomDTO.getCapacity().toString() + ")"));
    }

    private RoomDTO createExampleRoomDTO() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(10);
        return roomDTO;
    }

}
