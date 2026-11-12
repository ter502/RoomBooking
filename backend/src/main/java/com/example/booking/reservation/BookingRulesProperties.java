package com.example.booking.reservation;

import java.time.Duration;
import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking.rules")
public record BookingRulesProperties(
        int slotMinutes,
        LocalTime openTime,
        LocalTime closeTime,
        Duration maxDuration,
        int maxDaysAhead) {
}