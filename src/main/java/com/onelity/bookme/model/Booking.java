package com.onelity.bookme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity(name = "bookings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_id;
    private Integer room_number;
    private String title;
    private String description;
    private OffsetDateTime start_timestamp;
    private OffsetDateTime end_timestamp;
    private Integer participants;
    private String repeat_pattern;

    @ManyToMany(mappedBy = "bookings")
    @JsonIgnore
    private List<Room> rooms;

    public Long getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(Long booking_id) {
        this.booking_id = booking_id;
    }

    public Integer getRoom_number() {
        return room_number;
    }

    public void setRoom_number(Integer room_number) {
        this.room_number = room_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getStart_timestamp() {
        return start_timestamp;
    }

    public void setStart_timestamp(OffsetDateTime start_timestamp) {
        this.start_timestamp = start_timestamp;
    }

    public OffsetDateTime getEnd_timestamp() {
        return end_timestamp;
    }

    public void setEnd_timestamp(OffsetDateTime end_timestamp) {
        this.end_timestamp = end_timestamp;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public String getRepeat_pattern() {
        return repeat_pattern;
    }

    public void setRepeat_pattern(String repeat_pattern) {
        this.repeat_pattern = repeat_pattern;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
