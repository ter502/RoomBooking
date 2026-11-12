package com.example.booking.reservation;

import com.example.booking.room.Room;
import com.example.booking.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "series_id")
    private Long seriesId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Booking() {
    }

    public Booking(Room room, AppUser user, String title, Instant startAt, Instant endAt) {
        this.room = room;
        this.user = user;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = BookingStatus.CONFIRMED;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public AppUser getUser() { return user; }
    public Long getSeriesId() { return seriesId; }
    public String getTitle() { return title; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public BookingStatus getStatus() { return status; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}