package com.example.booking.reservation;

import java.time.Instant;
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
}