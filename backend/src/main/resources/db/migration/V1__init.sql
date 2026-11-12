CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keycloak_sub  VARCHAR(64)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE room (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(50)  NOT NULL UNIQUE,
    floor          VARCHAR(20)  NOT NULL,
    capacity       INTEGER      NOT NULL CHECK (capacity > 0),
    has_projector  BOOLEAN      NOT NULL DEFAULT false,
    has_video      BOOLEAN      NOT NULL DEFAULT false,
    active         BOOLEAN      NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE booking_series (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES app_user (id),
    room_id      BIGINT       NOT NULL REFERENCES room (id),
    title        VARCHAR(100) NOT NULL,
    day_of_week  SMALLINT     NOT NULL CHECK (day_of_week BETWEEN 1 AND 5),
    start_time   TIME         NOT NULL,
    end_time     TIME         NOT NULL,
    first_date   DATE         NOT NULL,
    last_date    DATE         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT series_time_order CHECK (end_time > start_time),
    CONSTRAINT series_date_order CHECK (last_date >= first_date)
);

CREATE TABLE booking (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id        BIGINT       NOT NULL REFERENCES room (id),
    user_id        BIGINT       NOT NULL REFERENCES app_user (id),
    series_id      BIGINT       REFERENCES booking_series (id),
    title          VARCHAR(100) NOT NULL,
    start_at       TIMESTAMPTZ  NOT NULL,
    end_at         TIMESTAMPTZ  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    checked_in_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT booking_status_valid
        CHECK (status IN ('CONFIRMED', 'CHECKED_IN', 'CANCELLED', 'RELEASED')),
    CONSTRAINT booking_time_order CHECK (end_at > start_at),
    CONSTRAINT booking_no_overlap EXCLUDE USING gist (
        room_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
    ) WHERE (status IN ('CONFIRMED', 'CHECKED_IN'))
);

CREATE INDEX idx_booking_user_start ON booking (user_id, start_at);
CREATE INDEX idx_booking_series ON booking (series_id);
CREATE INDEX idx_booking_confirmed_start ON booking (start_at) WHERE status = 'CONFIRMED';