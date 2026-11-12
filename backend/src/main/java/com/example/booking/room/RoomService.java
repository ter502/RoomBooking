package com.example.booking.room;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listActive(int minCapacity) {
        return roomRepository.findByActiveTrueAndCapacityGreaterThanEqualOrderByNameAsc(minCapacity)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        return RoomResponse.from(findRoom(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room name already exists");
        }
        Room room = new Room(request.name(), request.floor(), request.capacity(),
                request.projector(), request.video());
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = findRoom(id);
        if (!room.getName().equals(request.name()) && roomRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room name already exists");
        }
        boolean active = request.active() != null ? request.active() : room.isActive();
        room.update(request.name(), request.floor(), request.capacity(),
                request.projector(), request.video(), active);
        return RoomResponse.from(room);
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }
}