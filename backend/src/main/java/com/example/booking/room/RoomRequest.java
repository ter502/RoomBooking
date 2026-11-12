package com.example.booking.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 20) String floor,
        @NotNull @Min(1) @Max(200) Integer capacity,
        Boolean hasProjector,
        Boolean hasVideo,
        Boolean active) {
        
    boolean projector() {
        return Boolean.TRUE.equals(hasProjector);
    }

    boolean video() {
        return Boolean.TRUE.equals(hasVideo);
    }
}
