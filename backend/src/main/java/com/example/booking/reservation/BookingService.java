package com.example.booking.reservation;

import com.example.booking.common.PageResponse;
import com.example.booking.config.TimeConfig;
import com.example.booking.room.Room;
import com.example.booking.room.RoomRepository;
import com.example.booking.room.RoomResponse;
import com.example.booking.user.AppUser;
import com.example.booking.user.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RANGE_DAYS = 60;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CurrentUserService currentUserService;
    private final BookingRules bookingRules;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          CurrentUserService currentUserService, BookingRules bookingRules,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.currentUserService = currentUserService;
        this.bookingRules = bookingRules;
        this.clock = clock;
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

    @Transactional
    public BookingResponse get(Long id) {
        return BookingResponse.from(findOwnBooking(id));
    }

    @Transactional
    public PageResponse<BookingResponse> mine(LocalDate from, LocalDate to, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be >= 0 and size must be between 1 and " + MAX_PAGE_SIZE);
        }
        AppUser user = currentUserService.currentUser();

        LocalDate startDate = from != null ? from : LocalDate.now(clock);
        LocalDate endDate = to != null ? to : startDate.plusDays(DEFAULT_RANGE_DAYS);
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to must not be before from");
        }
        Instant fromInstant = startDate.atStartOfDay(TimeConfig.ZONE).toInstant();
        Instant toInstant = endDate.plusDays(1).atStartOfDay(TimeConfig.ZONE).toInstant();

        return PageResponse.from(bookingRepository
                .findByUserIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                        user.getId(), fromInstant, toInstant, PageRequest.of(page, size))
                .map(BookingResponse::from));
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = findOwnBooking(id);
        Instant now = clock.instant();
        if (!booking.getStartAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bookings that have already started cannot be cancelled");
        }
        int updated = bookingRepository.updateStatusIfCurrent(
                id, BookingStatus.CONFIRMED, BookingStatus.CANCELLED, now);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only confirmed bookings can be cancelled");
        }
        return bookingRepository.findById(id)
                .map(BookingResponse::from)
                .orElseThrow();
    }

    @Transactional
    public List<RoomScheduleItem> roomSchedule(Long roomId, LocalDate date) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        AppUser user = currentUserService.currentUser();
        LocalDate day = date != null ? date : LocalDate.now(clock);
        Instant from = day.atStartOfDay(TimeConfig.ZONE).toInstant();
        Instant to = day.plusDays(1).atStartOfDay(TimeConfig.ZONE).toInstant();

        return bookingRepository.findRoomSchedule(roomId, BookingStatus.OCCUPYING, from, to)
                .stream()
                .map(b -> RoomScheduleItem.from(b, b.getUser().getId().equals(user.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> availableRooms(OffsetDateTime start, OffsetDateTime end, int capacity) {
        if (capacity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "capacity must be at least 1");
        }
        Instant s = start.toInstant();
        Instant e = end.toInstant();
        bookingRules.validate(s, e);

        return bookingRepository.findAvailableRooms(capacity, BookingStatus.OCCUPYING, s, e)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    private Booking findOwnBooking(Long id) {
        AppUser user = currentUserService.currentUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only access your own bookings");
        }
        return booking;
    }
}