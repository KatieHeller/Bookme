package com.onelity.bookme.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.CustomUserDetails;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.service.CustomUserDetailsService;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
        createRoomInDatabase();
    }

    @AfterEach
    void teardown() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidBookingObject_whenCreateBooking_thenReturnSavedBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        // when
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        response.andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$.room", is(bookingDTO.getRoom())))
                .andExpect(jsonPath("$.title", is(bookingDTO.getTitle())))
                .andExpect(jsonPath("$.description", is(bookingDTO.getDescription())))
                .andExpect(jsonPath("$.startDate", is(bookingDTO.getStartDate().toString())))
                .andExpect(jsonPath("$.endDate", is(bookingDTO.getEndDate().toString())))
                .andExpect(jsonPath("$.startTime", is(bookingDTO.getStartTime().toString())))
                .andExpect(jsonPath("$.endTime", is(bookingDTO.getEndTime().toString())))
                .andExpect(jsonPath("$.participants", is(bookingDTO.getParticipants())))
                .andExpect(jsonPath("$.repeat_pattern", is(bookingDTO.getRepeat_pattern())));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidBookingObject_whenCreateBooking_thenDatabaseContainsBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        // when
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        List<Booking> bookings = bookingRepository.findAll();
        // then
        Assert.isTrue(bookings.size() == 1);
        Booking booking = bookings.get(0);
        Assert.isTrue(booking.getTitle().equals(bookingDTO.getTitle()));
        Assert.isTrue(booking.getDescription().equals(bookingDTO.getDescription()));
        Assert.isTrue(booking.getRoom().getName().equals(bookingDTO.getRoom()));
        Assert.isTrue(booking.getStartDate().equals(bookingDTO.getStartDate()));
        Assert.isTrue(booking.getEndDate().equals(bookingDTO.getEndDate()));
        Assert.isTrue(booking.getStartTime().equals(bookingDTO.getStartTime()));
        Assert.isTrue(booking.getEndTime().equals(bookingDTO.getEndTime()));
        Assert.isTrue(booking.getParticipants().equals(bookingDTO.getParticipants()));
        Assert.isTrue(booking.getRepeat_pattern() == null);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenInvalidBookingObject_whenCreateBooking_thenReturnBadRequest() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        bookingDTO.setParticipants(-100);
        // when
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        response.andDo(print()).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Participants cannot be less than 0"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenInvalidBookingObject_whenCreateBooking_thenDatabaseDoesNotContainRoom() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        bookingDTO.setParticipants(-100);
        // when
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        List<Booking> bookings = bookingRepository.findAll();
        Assert.isTrue(bookings.size() == 0);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingWithConflict_whenCreateBooking_thenReturnConflict() throws Exception {
        // given
        BookingDTO bookingDTO1 = createValidBookingDTO();
        BookingDTO bookingDTO2 = createValidBookingDTO();
        bookingDTO2.setTitle("Booking 2");
        bookingDTO2.setStartTime(new Time(07, 30, 00));
        bookingDTO2.setEndTime(new Time(12, 00, 00));
        // when
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO1)));
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO2)));
        // then
        String expectedString = "Meeting room with name " + bookingDTO1.getRoom() + " is already booked "
                + "for the same time";
        response.andDo(print()).andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(expectedString));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenBookingWithConflict_whenCreateBooking_thenDatabaseDoesNotContainNewBooking() throws Exception {
        // given
        BookingDTO bookingDTO1 = createValidBookingDTO();
        BookingDTO bookingDTO2 = createValidBookingDTO();
        bookingDTO2.setTitle("Booking 2");
        bookingDTO2.setStartTime(new Time(07, 30, 00));
        bookingDTO2.setEndTime(new Time(12, 00, 00));
        // when
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO1)));
        ResultActions response = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO2)));
        // then
        List<Booking> bookings = bookingRepository.findAll();
        Assert.isTrue(bookings.size() == 1);
        Booking booking = bookings.get(0);
        Assert.isTrue(booking.getTitle().equals(bookingDTO1.getTitle()));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenGetBooking_thenReturnBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        // when
        ResultActions response = mockMvc.perform(get("/bookings/{id}", id));
        // then
        response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.room", is(bookingDTO.getRoom())))
                .andExpect(jsonPath("$.title", is(bookingDTO.getTitle())))
                .andExpect(jsonPath("$.description", is(bookingDTO.getDescription())))
                .andExpect(jsonPath("$.startDate", is(bookingDTO.getStartDate().toString())))
                .andExpect(jsonPath("$.endDate", is(bookingDTO.getEndDate().toString())))
                .andExpect(jsonPath("$.startTime", is(bookingDTO.getStartTime().toString())))
                .andExpect(jsonPath("$.endTime", is(bookingDTO.getEndTime().toString())))
                .andExpect(jsonPath("$.participants", is(bookingDTO.getParticipants())))
                .andExpect(jsonPath("$.repeat_pattern", is(bookingDTO.getRepeat_pattern())));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNonexistentId_whenGetBooking_thenReturnNotFound() throws Exception {
        // given
        // Database is already empty so all ids will be nonexistent
        // when
        ResultActions response = mockMvc.perform(get("/bookings/{id}", 5L));
        // then
        String expectedString = "Booking with id " + 5L + " not found";
        response.andDo(print()).andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(expectedString));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void whenGetBookings_thenReturnBookings() throws Exception {
        // given
        BookingDTO bookingDTO1 = createValidBookingDTO();
        BookingDTO bookingDTO2 = createValidBookingDTO2();
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO1)));
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO2)));
        // when
        ResultActions response = mockMvc.perform(get("/bookings"));
        // then
        response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2)));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenDeleteBooking_thenSuccessfulDeletion() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", id));
        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenDeleteBooking_thenDatabaseDeletesBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", id));
        // then
        List<Booking> allBookings = bookingRepository.findAll();
        Assert.isTrue(allBookings.size() == 0);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNonexistentId_whenDeleteBooking_thenSuccessfulDeletion() throws Exception {
        // given
        // Database is empty so any id will be nonexistent
        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", 5L));
        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidBookingObject_whenUpdateBooking_thenReturnSavedBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        bookingDTO.setTitle("Updated Booking 1");
        // when
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.title", is(bookingDTO.getTitle())));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidBookingObject_whenUpdateBooking_thenDatabaseContainsUpdatedBooking() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        bookingDTO.setTitle("Updated Booking 1");
        // when
        mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        List<Booking> updatedBookings = bookingRepository.findAll();
        Assert.isTrue(updatedBookings.size() == 1);
        Booking updatedBooking = updatedBookings.get(0);
        Assert.isTrue(updatedBooking.getTitle().equals(bookingDTO.getTitle()));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingBookings_whenUpdateBooking_thenReturnConflict() throws Exception {
        // given
        BookingDTO bookingDTO1 = createValidBookingDTO();
        BookingDTO bookingDTO2 = createValidBookingDTO2();
        bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO1));
        Booking booking2 = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO2));
        Long id = booking2.getId();
        bookingDTO2.setStartTime(new Time(06, 00, 00));
        bookingDTO2.setEndTime(new Time(11, 00, 00));
        // when
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO2)));
        // then
        String expectedString = "Meeting room with name " + bookingDTO1.getRoom() + " is already booked "
                + "for the same time";
        response.andDo(print()).andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(expectedString));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingBookingObject_whenUpdateBooking_thenDatabaseRemainsSame() throws Exception {
        // given
        BookingDTO bookingDTO1 = createValidBookingDTO();
        BookingDTO bookingDTO2 = createValidBookingDTO2();
        bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO1));
        Booking booking2 = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO2));
        Long id = booking2.getId();
        bookingDTO2.setStartTime(new Time(06, 00, 00));
        bookingDTO2.setEndTime(new Time(11, 00, 00));
        // when
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO2)));
        // then
        List<Booking> existingBookings = bookingRepository.findAll();
        Assert.isTrue(existingBookings.size() == 2);
        booking2 = bookingRepository.getReferenceById(id);
        Assert.isTrue(booking2.getStartTime().equals(new Time(11, 00, 00)));
        Assert.isTrue(booking2.getEndTime().equals(new Time(14, 00, 00)));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenInvalidBookingObject_whenUpdateBooking_thenReturnBadRequest() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        bookingDTO.setParticipants(1000);
        // when
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        response.andDo(print()).andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.content()
                .string("Number of participants in booking exceeds meeting room capacity"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenInvalidBookingObject_whenUpdateBooking_thenDatabaseRemainsSame() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long id = booking.getId();
        bookingDTO.setParticipants(1000);
        // when
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));
        // then
        List<Booking> existingBookings = bookingRepository.findAll();
        Assert.isTrue(existingBookings.size() == 1);
        booking = existingBookings.get(0);
        Assert.isTrue(booking.getParticipants().equals(50));
    }

    @WithMockUser(username = "admin", roles = { "ADMIN" })
    private void createRoomInDatabase() {
        Room room = new Room();
        room.setName("Room 1");
        room.setLocation("Thessaloniki");
        room.setCapacity(100);
        roomRepository.saveAndFlush(room);
    }

    private BookingDTO createValidBookingDTO() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setTitle("Booking 1");
        bookingDTO.setRoom("Room 1");
        bookingDTO.setDescription("Description");
        bookingDTO.setStartDate(new Date(2003, 03, 01));
        bookingDTO.setEndDate(new Date(2003, 03, 01));
        bookingDTO.setStartTime(new Time(07, 00, 00));
        bookingDTO.setEndTime(new Time(10, 00, 00));
        bookingDTO.setParticipants(50);
        return bookingDTO;
    }

    private BookingDTO createValidBookingDTO2() {
        BookingDTO bookingDTO2 = new BookingDTO();
        bookingDTO2.setTitle("Booking 2");
        bookingDTO2.setRoom("Room 1");
        bookingDTO2.setDescription("Description");
        bookingDTO2.setStartDate(new Date(2003, 03, 01));
        bookingDTO2.setEndDate(new Date(2003, 03, 31));
        bookingDTO2.setStartTime(new Time(11, 00, 00));
        bookingDTO2.setEndTime(new Time(14, 00, 00));
        bookingDTO2.setParticipants(100);
        bookingDTO2.setRepeat_pattern("every day");
        return bookingDTO2;
    }

    private Booking convertBookingDTOToBooking(BookingDTO bookingDTO) {
        Room room = roomRepository.findByName(bookingDTO.getRoom());
        org.springframework.security.core.userdetails.User creator = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService
                .loadUserByUsername(creator.getUsername());
        return new Booking(bookingDTO.getId(), room, bookingDTO.getTitle(), bookingDTO.getDescription(),
                bookingDTO.getStartDate(), bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(),
                bookingDTO.getParticipants(), bookingDTO.getRepeat_pattern(), customUserDetails.getUser());
    }
}
