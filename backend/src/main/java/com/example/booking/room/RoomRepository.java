package com.example.booking.room;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);

    List<Room> findByActiveTrueAndCapacityGreaterThanEqualOrderByNameAsc(int capacity);
}