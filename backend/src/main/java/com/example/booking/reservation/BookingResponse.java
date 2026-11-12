package com.example.booking.reservation;

import com.example.booking.config.TimeConfig;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        Long roomId,
        String roomName,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BookingStatus status) {

    static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getRoom().getName(),
                booking.getTitle(),
                booking.getStartAt().atZone(TimeConfig.ZONE).toOffsetDateTime(),
                booking.getEndAt().atZone(TimeConfig.ZONE).toOffsetDateTime(),
                booking.getStatus());
    }
}