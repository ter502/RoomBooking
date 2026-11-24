package com.example.booking.reservation;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    CONFIRMED,
    CHECKED_IN,
    CANCELLED,
    RELEASED;

    // 會佔用會議室的狀態，必須和資料庫排除約束的 WHERE 條件一致
    public static final Set<BookingStatus> OCCUPYING =
            Collections.unmodifiableSet(EnumSet.of(CONFIRMED, CHECKED_IN));
}