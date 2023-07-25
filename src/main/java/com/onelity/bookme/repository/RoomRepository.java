package com.onelity.bookme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onelity.bookme.model.Room;

/**
 * Repository which handles accessing meeting rooms through the database and returning results to RoomService
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    public Room findByName(String name);
}
