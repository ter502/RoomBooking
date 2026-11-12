package com.example.booking.user;

public record MeResponse(Long id, String email, String displayName) {

    static MeResponse from(AppUser user) {
        return new MeResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}