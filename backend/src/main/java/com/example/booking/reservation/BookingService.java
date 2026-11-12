package com.example.booking.reservation;

import com.example.booking.room.Room;
import com.example.booking.room.RoomRepository;
import com.example.booking.user.AppUser;
import com.example.booking.user.CurrentUserService;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CurrentUserService currentUserService;
    private final BookingRules bookingRules;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          CurrentUserService currentUserService, BookingRules bookingRules) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.currentUserService = currentUserService;
        this.bookingRules = bookingRules;
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        AppUser user = currentUserService.currentUser();
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        if (!room.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available");
        }

        Instant start = request.startAt().toInstant();
        Instant end = request.endAt().toInstant();
        bookingRules.validate(start, end);

        Booking booking = bookingRepository.save(new Booking(room, user, request.title(), start, end));
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        return bookingRepository.findById(id)
                .map(BookingResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}