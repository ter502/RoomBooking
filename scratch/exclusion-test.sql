\set VERBOSITY verbose
BEGIN;

INSERT INTO app_user (keycloak_sub, email, display_name)
VALUES ('test-sub', 'test@example.com', 'Tester');
INSERT INTO room (name, floor, capacity) VALUES ('3A', '3F', 6);

SELECT id AS uid FROM app_user WHERE keycloak_sub = 'test-sub' \gset
SELECT id AS rid FROM room WHERE name = '3A' \gset

INSERT INTO booking (room_id, user_id, title, start_at, end_at)
VALUES (:rid, :uid, 'weekly', '2026-11-10 10:00+08', '2026-11-10 11:00+08');

\echo '--- Test A: overlap (should fail) ---'
SAVEPOINT a;
INSERT INTO booking (room_id, user_id, title, start_at, end_at)
VALUES (:rid, :uid, 'overlap', '2026-11-10 10:30+08', '2026-11-10 11:30+08');
ROLLBACK TO SAVEPOINT a;

\echo '--- Test B: adjacent (should succeed) ---'
INSERT INTO booking (room_id, user_id, title, start_at, end_at)
VALUES (:rid, :uid, 'next', '2026-11-10 11:00+08', '2026-11-10 12:00+08');

\echo '--- Test C: cancel then rebook (should succeed) ---'
UPDATE booking SET status = 'CANCELLED' WHERE title = 'weekly';
INSERT INTO booking (room_id, user_id, title, start_at, end_at)
VALUES (:rid, :uid, 'rebook', '2026-11-10 10:00+08', '2026-11-10 11:00+08');

ROLLBACK;