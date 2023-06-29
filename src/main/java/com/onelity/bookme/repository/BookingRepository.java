package com.onelity.bookme.repository;

import com.onelity.bookme.model.Booking;
import com.onelity.bookme.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Date;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByRoom(Room room);

    @Query("SELECT DISTINCT b FROM bookings b WHERE :room = b.room AND " +
            ":endTime > b.startTime AND :startTime < b.endTime AND " +
            ":endDate >= b.startDate AND :startDate <= b.endDate")
    public List<Booking> findOverlappingBookingsCreate(@Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                                 @Param("startTime") Time startTime, @Param("endTime") Time endTime,
                                                 @Param("room") Room room);

    @Query("SELECT DISTINCT b FROM bookings b WHERE :room = b.room AND " +
            ":endTime > b.startTime AND :startTime < b.endTime AND " +
            ":endDate >= b.startDate AND :startDate <= b.endDate AND " +
            ":id <> b.id")
    public List<Booking> findOverlappingBookingsUpdate(@Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                                 @Param("startTime") Time startTime, @Param("endTime") Time endTime,
                                                 @Param("room") Room room, @Param("id") Long id);
}
