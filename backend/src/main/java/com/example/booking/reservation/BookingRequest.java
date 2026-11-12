package com.example.booking.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record BookingRequest(
        @NotNull Long roomId,
        @NotBlank @Size(max = 100) String title,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt) {
}