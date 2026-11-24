package com.example.booking.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.booking.config.TimeConfig;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class BookingRulesTest {

    // 把「現在」固定在 2026-11-25（星期三）09:00 台北時間
    private static final Clock FIXED_CLOCK = Clock.fixed(at("2026-11-25T09:00"), TimeConfig.ZONE);

    private final BookingRules rules = new BookingRules(
            new BookingRulesProperties(15, LocalTime.of(8, 0), LocalTime.of(20, 0),
                    Duration.ofHours(4), 60),
            FIXED_CLOCK);

    private static Instant at(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(TimeConfig.ZONE).toInstant();
    }

    private void assertAccepted(String start, String end) {
        assertThatCode(() -> rules.validate(at(start), at(end)))
                .doesNotThrowAnyException();
    }

    private void assertRejected(String start, String end, String expectedMessage) {
        assertThatThrownBy(() -> rules.validate(at(start), at(end)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void acceptsValidBooking() {
        assertAccepted("2026-11-26T10:00", "2026-11-26T11:00");
    }

    @Test
    void rejectsEndBeforeStart() {
        assertRejected("2026-11-26T11:00", "2026-11-26T10:00", "End time must be after start time");
    }

    @Test
    void rejectsTimeNotOnSlotBoundary() {
        assertRejected("2026-11-26T10:10", "2026-11-26T11:00", "15-minute boundaries");
    }

    @Test
    void rejectsBookingSpanningTwoDays() {
        assertRejected("2026-11-26T19:00", "2026-11-27T09:00", "same day");
    }

    @Test
    void rejectsWeekend() {
        assertRejected("2026-11-28T10:00", "2026-11-28T11:00", "weekdays");
    }

    @Test
    void rejectsStartBeforeOpening() {
        assertRejected("2026-11-26T07:45", "2026-11-26T08:30", "between 08:00 and 20:00");
    }

    @Test
    void rejectsEndAfterClosing() {
        assertRejected("2026-11-26T19:30", "2026-11-26T20:15", "between 08:00 and 20:00");
    }

    @Test
    void acceptsBookingEndingExactlyAtClosing() {
        assertAccepted("2026-11-26T19:00", "2026-11-26T20:00");
    }

    @Test
    void rejectsLongerThanMaxDuration() {
        assertRejected("2026-11-26T09:00", "2026-11-26T13:15", "longer than 4 hours");
    }

    @Test
    void acceptsExactlyMaxDuration() {
        assertAccepted("2026-11-26T10:00", "2026-11-26T14:00");
    }

    @Test
    void rejectsPastTime() {
        assertRejected("2026-11-24T10:00", "2026-11-24T11:00", "in the past");
    }

    @Test
    void rejectsBookingStartingExactlyNow() {
        assertRejected("2026-11-25T09:00", "2026-11-25T10:00", "in the past");
    }

    @Test
    void rejectsTooFarAhead() {
        assertRejected("2027-01-25T10:00", "2027-01-25T11:00", "more than 60 days ahead");
    }
}