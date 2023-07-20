package com.onelity.bookme.repository;

import com.onelity.bookme.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository which handles accessing meeting rooms through the database and returning results to RoomService
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    public Room findByName(String name);
}