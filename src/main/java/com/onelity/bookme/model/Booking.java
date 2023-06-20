package com.onelity.bookme.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity(name = "bookings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private String title;
    private String description;
    private OffsetDateTime start_timestamp;
    private OffsetDateTime end_timestamp;
    private Integer participants;
    private String repeat_pattern;

    public Booking() {
    }

    public Long getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(Long booking_id) {
        this.booking_id = booking_id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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
}
