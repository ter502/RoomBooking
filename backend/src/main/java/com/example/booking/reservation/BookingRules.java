package com.example.booking.reservation;

import com.example.booking.config.TimeConfig;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class BookingRules {

    private final BookingRulesProperties props;
    private final Clock clock;

    public BookingRules(BookingRulesProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    public void validate(Instant start, Instant end) {
        ZonedDateTime s = start.atZone(TimeConfig.ZONE);
        ZonedDateTime e = end.atZone(TimeConfig.ZONE);

        if (!e.isAfter(s)) {
            fail("End time must be after start time");
        }
        if (!isAligned(s) || !isAligned(e)) {
            fail("Times must be on " + props.slotMinutes() + "-minute boundaries");
        }
        if (!s.toLocalDate().equals(e.toLocalDate())) {
            fail("Booking must start and end on the same day");
        }
        if (s.getDayOfWeek() == DayOfWeek.SATURDAY || s.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fail("Bookings are only allowed on weekdays");
        }
        if (s.toLocalTime().isBefore(props.openTime()) || e.toLocalTime().isAfter(props.closeTime())) {
            fail("Bookings must be between " + props.openTime() + " and " + props.closeTime());
        }
        if (Duration.between(start, end).compareTo(props.maxDuration()) > 0) {
            fail("Booking cannot be longer than " + props.maxDuration().toHours() + " hours");
        }
        Instant now = clock.instant();
        if (!start.isAfter(now)) {
            fail("Cannot book a time in the past");
        }
        if (start.isAfter(now.plus(Duration.ofDays(props.maxDaysAhead())))) {
            fail("Cannot book more than " + props.maxDaysAhead() + " days ahead");
        }
    }

    private boolean isAligned(ZonedDateTime t) {
        return t.getSecond() == 0 && t.getNano() == 0 && t.getMinute() % props.slotMinutes() == 0;
    }

    private static void fail(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}