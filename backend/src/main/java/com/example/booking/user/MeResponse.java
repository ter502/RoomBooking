package com.example.booking.user;

import java.util.List;

public record MeResponse(Long id, String email, String displayName, List<String> roles) {

    static MeResponse from(AppUser user, List<String> roles) {
        return new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(), roles);
    }
}