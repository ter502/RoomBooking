package com.example.booking.reservation;

import com.example.booking.config.TimeConfig;
import java.time.OffsetDateTime;

public record RoomScheduleItem(
        Long bookingId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BookingStatus status,
        String organizer,
        String title) {

    static RoomScheduleItem from(Booking booking, boolean showTitle) {
        return new RoomScheduleItem(
                booking.getId(),
                booking.getStartAt().atZone(TimeConfig.ZONE).toOffsetDateTime(),
                booking.getEndAt().atZone(TimeConfig.ZONE).toOffsetDateTime(),
                booking.getStatus(),
                booking.getUser().getDisplayName(),
                showTitle ? booking.getTitle() : null);
    }
}