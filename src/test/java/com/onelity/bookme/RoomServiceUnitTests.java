package com.onelity.bookme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.onelity.bookme.exception.InvalidRoomException;
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
    public void givenLongName_whenCreateRoom_thenThrowsInvalidRoomException() throws Exception {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setName("Room 1 xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Name cannot be more than 100 characters", thrown.getMessage());
    }

    @Test
    public void givenInvalidLocation_whenCreateRoom_thenThrowsInvalidRoomException() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setLocation("Greece");
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Location must be either 'Thessaloniki' or 'Cologne'", thrown.getMessage());
    }

    @Test
    public void givenNegativeCapacity_whenCreateRoom_thenThrowsInvalidRoomException() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setCapacity(-10);
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Capacity cannot be less than 0", thrown.getMessage());
    }

    @Test
    public void givenNullRoom_whenCreateRoom_thenThrowsInvalidRoomException() {
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(null),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Room can not be null", thrown.getMessage());
    }

    @Test
    public void givenNullRoomName_whenCreateRoom_thenThrowsInvalidRoomException() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setName(null);
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Room fields can not be null", thrown.getMessage());
    }

    @Test
    public void givenNullLocation_whenCreateRoom_thenThrowsInvalidRoomException() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setLocation(null);
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Room fields can not be null", thrown.getMessage());
    }

    @Test
    public void givenNullCapacity_whenCreateRoom_thenThrowsInvalidRoomException() {
        RoomDTO roomDTO = createExampleRoomDTO();
        roomDTO.setCapacity(null);
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.createRoomInDatabase(roomDTO),
                "Expected createRoomInDatabase() to throw, but it didn't");
        assertEquals("Room fields can not be null", thrown.getMessage());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNewRoomCapacityBelowBookingParticipants_whenUpdateRoom_thenReturnBadRequest() throws Exception {
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
        ResponseEntity<RoomDTO> response = roomService.createRoomInDatabase(roomDTO);
        RoomDTO addedRoom = response.getBody();
        Long id = addedRoom.getId();
        bookingService.createBookingInDatabase(bookingDTO);
        roomDTO.setCapacity(5);
        InvalidRoomException thrown = assertThrows(InvalidRoomException.class,
                () -> roomService.updateRoomInDatabase(id, roomDTO),
                "Expected updateRoomInDatabase() to throw, but it didn't");
        assertEquals("Room could not be updated because booking with " + "title '" + bookingDTO.getTitle()
                + "' has more participants (" + bookingDTO.getParticipants().toString() + ") than new capacity ("
                + roomDTO.getCapacity().toString() + ")", thrown.getMessage());
    }

    private RoomDTO createExampleRoomDTO() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(10);
        return roomDTO;
    }

}
