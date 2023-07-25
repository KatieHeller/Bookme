package com.onelity.bookme.dto;

import java.sql.Date;
import java.sql.Time;

/**
 * DTO class for Booking entities
 */
public class BookingDTO {

    private Long id;
    private String room;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private Integer participants;
    private String repeat_pattern;

    public BookingDTO() {
    }

    public BookingDTO(Long id, String room, String title, String description, Date startDate, Date endDate,
            Time startTime, Time endTime, Integer participants, String repeat_pattern) {
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
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
}
