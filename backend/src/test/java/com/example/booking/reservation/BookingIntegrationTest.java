package com.example.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.booking.TestcontainersConfiguration;
import com.example.booking.common.GlobalExceptionHandler;
import com.example.booking.config.TimeConfig;
import com.example.booking.room.Room;
import com.example.booking.room.RoomRepository;
import com.example.booking.user.CurrentUserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CurrentUserService currentUserService;

    private Room room;
    private LocalDate day;

    @BeforeEach
    void setUp() {
        // 先建立開發用使用者，避免並發測試時多個執行緒同時去建立它
        currentUserService.currentUser();
        room = roomRepository.save(new Room(
                "IT-" + UUID.randomUUID().toString().substring(0, 8), "9F", 6, false, false));
        // 下下週一：一定是平日、一定在未來、一定在 60 天內
        day = LocalDate.now(TimeConfig.ZONE).with(TemporalAdjusters.next(DayOfWeek.MONDAY)).plusWeeks(1);
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
    }

    private BookingRequest request(String title, int startHour, int startMinute,
                                   int endHour, int endMinute) {
        return new BookingRequest(room.getId(), title,
                at(startHour, startMinute), at(endHour, endMinute));
    }

    private OffsetDateTime at(int hour, int minute) {
        return day.atTime(LocalTime.of(hour, minute)).atZone(TimeConfig.ZONE).toOffsetDateTime();
    }

    @Test
    void overlappingBookingIsRejectedAndMappedTo409() {
        bookingService.create(request("First", 10, 0, 11, 0));

        assertThatThrownBy(() -> bookingService.create(request("Overlap", 10, 30, 11, 30)))
                .isInstanceOfSatisfying(DataIntegrityViolationException.class, ex -> {
                    ProblemDetail problem = new GlobalExceptionHandler().handleDataIntegrity(ex);
                    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(problem.getDetail()).isEqualTo("The room is already booked for this time");
                });
    }

    @Test
    void adjacentBookingsAreAllowed() {
        bookingService.create(request("First", 10, 0, 11, 0));

        BookingResponse next = bookingService.create(request("Next", 11, 0, 12, 0));

        assertThat(next.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancelledBookingFreesTheSlot() {
        BookingResponse first = bookingService.create(request("First", 10, 0, 11, 0));
        bookingService.cancel(first.id());

        BookingResponse rebooked = bookingService.create(request("Rebook", 10, 0, 11, 0));

        assertThat(rebooked.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void concurrentBookingsForSameSlotAllowOnlyOne() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                int n = i;
                results.add(executor.submit(() -> {
                    startGate.await();
                    try {
                        bookingService.create(request("Race " + n, 14, 0, 15, 0));
                        return true;
                    } catch (DataIntegrityViolationException e) {
                        return false;
                    }
                }));
            }
            startGate.countDown();

            int successes = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
            assertThat(successes).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }

        assertThat(bookingRepository.count()).isEqualTo(1);
    }
}