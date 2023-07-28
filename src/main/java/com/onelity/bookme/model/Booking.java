package com.onelity.bookme.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.sql.Date;
import java.sql.Time;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Model class for booking entities, which corresponds with 'bookings' table in database
 */
@Entity(name = "bookings")
@Table(name = "bookings", schema = "public")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Links a booking to its meeting room through a foreign key from this room column to the id column of the
     * 'meeting_rooms' table
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Room room;

    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private Integer participants;
    private String repeat_pattern;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User creator;

    public Booking() {
    }

    public Booking(Long id, Room room, String title, String description, Date startDate, Date endDate, Time startTime,
            Time endTime, Integer participants, String repeat_pattern, User user) {
        this.id = id;
        this.room = room;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participants = participants;
        this.repeat_pattern = repeat_pattern;
        this.creator = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
