package com.example.booking.reservation;

import com.example.booking.room.Room;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = "room")
    Page<Booking> findByUserIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
            Long userId, Instant from, Instant to, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Booking b
               set b.status = :newStatus, b.updatedAt = :now
             where b.id = :id and b.status = :expected
            """)
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("expected") BookingStatus expected,
                              @Param("newStatus") BookingStatus newStatus,
                              @Param("now") Instant now);

    @Query("""
            select b from Booking b
              join fetch b.user
             where b.room.id = :roomId
               and b.status in :statuses
               and b.startAt < :to
               and b.endAt > :from
             order by b.startAt
            """)
    List<Booking> findRoomSchedule(@Param("roomId") Long roomId,
                                   @Param("statuses") Collection<BookingStatus> statuses,
                                   @Param("from") Instant from,
                                   @Param("to") Instant to);

    @Query("""
            select r from Room r
             where r.active = true
               and r.capacity >= :capacity
               and not exists (
                   select b.id from Booking b
                    where b.room = r
                      and b.status in :statuses
                      and b.startAt < :end
                      and b.endAt > :start)
             order by r.name
            """)
    List<Room> findAvailableRooms(@Param("capacity") int capacity,
                                  @Param("statuses") Collection<BookingStatus> statuses,
                                  @Param("start") Instant start,
                                  @Param("end") Instant end);
}