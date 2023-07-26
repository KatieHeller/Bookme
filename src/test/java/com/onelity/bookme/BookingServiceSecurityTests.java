package com.onelity.bookme;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelity.bookme.dto.BookingDTO;
import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Location;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingServiceSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        createMeetingRoomInDatabase();
    }

    @AfterEach
    void teardown() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenBookingCreatorEmployee_whenUpdateBooking_thenReturnOk() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long bookingId = booking.getId();

        // when
        bookingDTO.setDescription("Updated description");
        ResultActions response = mockMvc.perform(put("/bookings/{id}", bookingId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bookingDTO)));

        // then
        response.andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenAdmin_whenUpdateBooking_thenReturnOk() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long bookingId = booking.getId();

        // when
        bookingDTO.setDescription("Updated description");
        ResultActions response = mockMvc.perform(put("/bookings/{id}", bookingId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bookingDTO)));

        // then
        response.andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenNonBookingCreatorEmployee_whenUpdateBooking_thenReturnUnauthorized() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)).with(user("other employee").roles("EMPLOYEE")));
        List<Booking> bookings = bookingRepository.findAll();
        Long id = bookings.get(0).getId();

        // when
        bookingDTO.setDescription("Updated description");
        ResultActions response = mockMvc.perform(put("/bookings/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenBookingCreatorEmployee_whenDeleteBooking_thenReturnNoContent() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long bookingId = booking.getId();

        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", bookingId));

        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenAdmin_whenDeleteBooking_thenReturnNoContent() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        Booking booking = bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
        Long bookingId = booking.getId();

        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", bookingId));

        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenNonBookingCreatorEmployee_whenDeleteBooking_thenReturnUnauthorized() throws Exception {
        // given
        BookingDTO bookingDTO = createValidBookingDTO();
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)).with(user("other employee").roles("EMPLOYEE")));
        List<Booking> bookings = bookingRepository.findAll();
        Long id = bookings.get(0).getId();

        // when
        ResultActions response = mockMvc.perform(delete("/bookings/{id}", id));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    private void createMeetingRoomInDatabase() {
        roomRepository.saveAndFlush(new Room("Room 1", "Thessaloniki", 50));
    }

    private Booking createBookingAsOtherEmployee() {
        BookingDTO bookingDTO = createValidBookingDTO();
        return bookingRepository.saveAndFlush(convertBookingDTOToBooking(bookingDTO));
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

    private Booking convertBookingDTOToBooking(BookingDTO bookingDTO) {
        Room room = roomRepository.findByName(bookingDTO.getRoom());
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        return new Booking(bookingDTO.getId(), room, bookingDTO.getTitle(), bookingDTO.getDescription(),
                bookingDTO.getStartDate(), bookingDTO.getEndDate(), bookingDTO.getStartTime(), bookingDTO.getEndTime(),
                bookingDTO.getParticipants(), bookingDTO.getRepeat_pattern(), username);
    }
}
