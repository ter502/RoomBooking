package com.example.booking.room;

public record RoomResponse(
        Long id,
        String name,
        String floor,
        int capacity,
        boolean hasProjector,
        boolean hasVideo,
        boolean active) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(), room.getName(), room.getFloor(), room.getCapacity(),
                room.isHasProjector(), room.isHasVideo(), room.isActive());
    }
}