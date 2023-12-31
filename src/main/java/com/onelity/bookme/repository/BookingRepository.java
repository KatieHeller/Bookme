package com.onelity.bookme.repository;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;

/**
 * Repository which handles accessing bookings through the database and returning results to BookingService
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByRoom(Room room);

    /**
     * Finds bookings in a certain room that overlap with a given booking's dates and times, considering all existing
     * bookings in database
     *
     * @return returns list of overlapping bookings
     */
    @Query("SELECT DISTINCT b FROM bookings b WHERE :room = b.room AND "
            + ":endTime > b.startTime AND :startTime < b.endTime AND "
            + ":endDate >= b.startDate AND :startDate <= b.endDate")
    public List<Booking> findOverlappingBookingsCreate(@Param("startDate") Date startDate,
            @Param("endDate") Date endDate, @Param("startTime") Time startTime, @Param("endTime") Time endTime,
            @Param("room") Room room);

    /**
     * Finds bookings in a certain room that overlap with a given booking's dates and times, ignoring the booking that
     * is being updated, which is allowed to overlap with its previous date and time
     *
     * @return returns list of overlapping bookings
     */
    @Query("SELECT DISTINCT b FROM bookings b WHERE :room = b.room AND "
            + ":endTime > b.startTime AND :startTime < b.endTime AND "
            + ":endDate >= b.startDate AND :startDate <= b.endDate AND " + ":id <> b.id")
    public List<Booking> findOverlappingBookingsUpdate(@Param("startDate") Date startDate,
            @Param("endDate") Date endDate, @Param("startTime") Time startTime, @Param("endTime") Time endTime,
            @Param("room") Room room, @Param("id") Long id);
}
