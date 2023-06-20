package com.onelity.bookme.dto;

import java.time.OffsetDateTime;

public class BookingDTO {

    private Long id;
    private String title;
    private OffsetDateTime start_timestamp;
    private OffsetDateTime end_timestamp;
    private Integer participants;
    private String repeat_pattern;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
