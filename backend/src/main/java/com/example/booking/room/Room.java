package com.example.booking.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "floor", nullable = false, length = 20)
    private String floor;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "has_projector", nullable = false)
    private boolean hasProjector;

    @Column(name = "has_video", nullable = false)
    private boolean hasVideo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Room() {
    }

    public Room(String name, String floor, int capacity, boolean hasProjector, boolean hasVideo) {
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.hasProjector = hasProjector;
        this.hasVideo = hasVideo;
    }

    public void update(String name, String floor, int capacity,
                       boolean hasProjector, boolean hasVideo, boolean active) {
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.hasProjector = hasProjector;
        this.hasVideo = hasVideo;
        this.active = active;
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
    public String getName() { return name; }
    public String getFloor() { return floor; }
    public int getCapacity() { return capacity; }
    public boolean isHasProjector() { return hasProjector; }
    public boolean isHasVideo() { return hasVideo; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}