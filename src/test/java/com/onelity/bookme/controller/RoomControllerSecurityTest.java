package com.onelity.bookme.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.BookingRepository;
import com.onelity.bookme.repository.RoomRepository;
import com.onelity.bookme.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerSecurityTest {

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

    @BeforeTestClass
    void classSetup() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void teardown() {
        roomRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenAdmin_whenCreateRoom_thenReturnCreated() throws Exception {
        // given
        RoomDTO roomDTO = createValidRoomDTO();

        // when
        ResultActions response = mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then
        response.andDo(print()).andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenEmployee_whenCreateRoom_theReturnUnauthorized() throws Exception {
        // given
        RoomDTO roomDTO = createValidRoomDTO();

        // when
        ResultActions response = mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenAdmin_whenDeleteRoom_thenReturnNoContent() throws Exception {
        // given
        Room room = roomRepository.saveAndFlush(modelMapper.map(createValidRoomDTO(), Room.class));
        Long id = room.getId();

        // when
        ResultActions response = mockMvc.perform(delete("/meeting-rooms/{id}", id));

        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenEmployee_whenDeleteRoom_thenReturnUnauthorized() throws Exception {
        // given
        Room room = roomRepository.saveAndFlush(modelMapper.map(createValidRoomDTO(), Room.class));
        Long id = room.getId();

        // when
        ResultActions response = mockMvc.perform(delete("/meeting-rooms/{id}", id));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenAdmin_whenUpdate_thenReturnOk() throws Exception {
        // given
        Room room = roomRepository.saveAndFlush(modelMapper.map(createValidRoomDTO(), Room.class));
        Long id = room.getId();

        // when
        RoomDTO roomDTO = createValidRoomDTO();
        roomDTO.setName("Updated name");
        ResultActions response = mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then
        response.andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenEmployee_whenUpdate_thenReturnUnauthorized() throws Exception {
        // given
        Room room = roomRepository.saveAndFlush(modelMapper.map(createValidRoomDTO(), Room.class));
        Long id = room.getId();

        // when
        RoomDTO roomDTO = createValidRoomDTO();
        roomDTO.setName("Updated name");
        ResultActions response = mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void givenAnonymous_whenGetRooms_thenReturnUnauthorized() throws Exception {
        // given

        // when
        ResultActions response = mockMvc.perform(get("/meeting-rooms"));

        // then
        response.andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    public void givenEmployee_whenGetRooms_thenReturnOk() throws Exception {
        // given

        // when
        ResultActions response = mockMvc.perform(get("/meeting-rooms"));

        // then
        response.andDo(print()).andExpect(status().isOk());
    }

    private RoomDTO createValidRoomDTO() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(40);
        return roomDTO;
    }

}
