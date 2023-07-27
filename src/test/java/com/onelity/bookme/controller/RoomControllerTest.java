package com.onelity.bookme.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelity.bookme.dto.RoomDTO;
import com.onelity.bookme.model.Location;
import com.onelity.bookme.model.Room;
import com.onelity.bookme.repository.RoomRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @BeforeTestClass
    void setup() {
        roomRepository.deleteAll();
    }

    @AfterEach
    void teardown() {
        roomRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenRoomObject_whenCreateRoom_thenReturnSavedRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$.name", is(roomDTO.getName())))
                .andExpect(jsonPath("$.location", is(roomDTO.getLocation())))
                .andExpect(jsonPath("$.capacity", is(roomDTO.getCapacity())));

    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidRoomObject_whenCreateRoom_thenDatabaseContainsRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();

        // when - action or behavior we are going to test
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));
        Room room = roomRepository.findByName(roomDTO.getName());

        // then - verify the result or output using assert statements
        Assert.notNull(room);
        Assert.isTrue(room.getName().equals(roomDTO.getName()));
        Assert.isTrue(room.getLocation().equals(roomDTO.getLocation()));
        Assert.isTrue(room.getCapacity().equals(roomDTO.getCapacity()));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingRoomObject_whenCreateRoom_thenReturnConflict() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();

        // when - action or behavior we are going to test
        // try to put the same room in twice
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));
        ResultActions response = mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingRoomObject_whenCreateRoom_thenDatabaseDoesNotContainDuplicateRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();

        // when - action or behavior we are going to test
        // try to put the same room in twice
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)));

        // then - verify the result or output using assert statements
        List<Room> rooms = roomRepository.findAll();
        Assert.isTrue(rooms.size() == 1);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenGetRoom_thenReturnRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO, Room.class);
        Room newRoom = roomRepository.saveAndFlush(room);
        Long id = newRoom.getId();

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(get("/meeting-rooms/{id}", id));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(Math.toIntExact(room.getId()))))
                .andExpect(jsonPath("$.name", is(roomDTO.getName())))
                .andExpect(jsonPath("$.location", is(roomDTO.getLocation())))
                .andExpect(jsonPath("$.capacity", is(roomDTO.getCapacity())));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNonexistentId_whenGetRoom_thenReturnNotFound() throws Exception {

        // given - precondition or setup
        // Database is already empty so all ids will be nonexistent

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(get("/meeting-rooms/{id}", 5L));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void whenGetRooms_thenReturnRooms() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO1 = createValidRoomDTO();
        RoomDTO roomDTO2 = createValidRoomDTO2();
        RoomDTO roomDTO3 = new RoomDTO();
        roomDTO3.setName("Room 3");
        roomDTO3.setLocation("Thessaloniki");
        roomDTO3.setCapacity(100);
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO1)));
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO2)));
        mockMvc.perform(post("/meeting-rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO3)));

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(get("/meeting-rooms"));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(3)));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenDeleteRoom_thenSuccessfulDeletion() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO, Room.class);
        Room newRoom = roomRepository.saveAndFlush(room);
        Long id = newRoom.getId();

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(delete("/meeting-rooms/{id}", id));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenExistingId_whenDeleteRoom_thenDatabaseDeletesRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO, Room.class);
        Room newRoom = roomRepository.saveAndFlush(room);
        Long id = newRoom.getId();

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(delete("/meeting-rooms/{id}", id));

        // then - verify the result or output using assert statements
        List<Room> allRooms = roomRepository.findAll();
        Assert.isTrue(allRooms.size() == 0);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenNonexistentId_whenDeleteRoom_thenSuccessfulDeletion() throws Exception {

        // given - precondition or setup
        // Database is empty so any id will be nonexistent

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(delete("/meeting-rooms/{id}", 5L));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidRoomObject_whenUpdateRoom_thenReturnSavedRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO, Room.class);
        Room newRoom = roomRepository.saveAndFlush(room);
        Long id = newRoom.getId();
        RoomDTO updatedRoomDTO = new RoomDTO();
        updatedRoomDTO.setName("Updated Room 1");
        updatedRoomDTO.setLocation("Cologne");
        updatedRoomDTO.setCapacity(50);

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRoomDTO)));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.name", is(updatedRoomDTO.getName())))
                .andExpect(jsonPath("$.location", is(updatedRoomDTO.getLocation())))
                .andExpect(jsonPath("$.capacity", is(updatedRoomDTO.getCapacity())));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenValidRoomObject_whenUpdateRoom_thenDatabaseContainsUpdatedRoom() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO, Room.class);
        Room newRoom = roomRepository.saveAndFlush(room);
        Long id = newRoom.getId();
        RoomDTO updatedRoomDTO = new RoomDTO();
        updatedRoomDTO.setName("Updated Room 1");
        updatedRoomDTO.setLocation("Cologne");
        updatedRoomDTO.setCapacity(50);

        // when - action or behavior we are going to test
        mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRoomDTO)));

        // then - verify the result or output using assert statements
        List<Room> updatedRooms = roomRepository.findAll();
        Assert.isTrue(updatedRooms.size() == 1);
        Room updatedRoom = updatedRooms.get(0);
        Assert.isTrue(updatedRoom.getName().equals(updatedRoomDTO.getName()));
        Assert.isTrue(updatedRoom.getLocation().equals(updatedRoomDTO.getLocation()));
        Assert.isTrue(updatedRoom.getCapacity().equals(updatedRoomDTO.getCapacity()));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingRoomObject_whenUpdateRoom_thenReturnConflict() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO1 = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO1, Room.class);
        roomRepository.saveAndFlush(room);
        RoomDTO roomDTO2 = createValidRoomDTO2();
        Room room2 = roomRepository.saveAndFlush(modelMapper.map(roomDTO2, Room.class));
        Long id = room2.getId();
        // set the name of roomDTO2 to be "Room 1" which already exists in database
        roomDTO2.setName("Room 1");

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO2)));

        // then - verify the result or output using assert statements
        response.andDo(print()).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void givenConflictingRoomObject_whenUpdateRoom_thenDatabaseRemainsSame() throws Exception {

        // given - precondition or setup
        RoomDTO roomDTO1 = createValidRoomDTO();
        Room room = modelMapper.map(roomDTO1, Room.class);
        roomRepository.saveAndFlush(room);
        RoomDTO roomDTO2 = createValidRoomDTO2();
        Room room2 = roomRepository.saveAndFlush(modelMapper.map(roomDTO2, Room.class));
        Long id = room2.getId();
        // set the name of roomDTO2 to be "Room 1" which already exists in database
        roomDTO2.setName("Room 1");

        // when - action or behavior we are going to test
        ResultActions response = mockMvc.perform(put("/meeting-rooms/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO2)));

        // then - verify the result or output using assert statements
        List<Room> existingRooms = roomRepository.findAll();
        Assert.isTrue(existingRooms.size() == 2);
        room2 = roomRepository.getReferenceById(id);
        Assert.isTrue(room2.getName().equals("Room 2"));
    }

    private RoomDTO createValidRoomDTO() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName("Room 1");
        roomDTO.setLocation("Thessaloniki");
        roomDTO.setCapacity(40);
        return roomDTO;
    }

    private RoomDTO createValidRoomDTO2() {
        RoomDTO roomDTO2 = new RoomDTO();
        roomDTO2.setName("Room 2");
        roomDTO2.setLocation("Cologne");
        roomDTO2.setCapacity(30);
        return roomDTO2;
    }

}
