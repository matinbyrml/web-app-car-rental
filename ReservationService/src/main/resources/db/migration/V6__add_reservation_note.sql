DROP TABLE IF EXISTS reservation_note;
DROP TABLE IF EXISTS reservation_note_notes;
DROP TABLE IF EXISTS reservation_note_damages;

-- Create reservation_note table
CREATE TABLE reservation_note
(
    id                BIGSERIAL PRIMARY KEY,
    reservation_id    BIGINT                   NOT NULL,
    vehicle_id        BIGINT                   NOT NULL,
    start_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at            TIMESTAMP WITH TIME ZONE,
    km_at_pickup      INT                      NOT NULL,
    km_at_return      INT,
    cleanliness       VARCHAR(255),
    needs_maintenance BOOLEAN,
    CONSTRAINT fk_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT fk_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
);

-- Create reservation_note_notes table
CREATE TABLE reservation_note_notes
(
    reservation_note_id BIGINT NOT NULL,
    note                TEXT,
    PRIMARY KEY (reservation_note_id, note),
    CONSTRAINT fk_reservation_note FOREIGN KEY (reservation_note_id) REFERENCES reservation_note (id)
);

-- Create reservation_note_damages table
CREATE TABLE reservation_note_damages
(
    reservation_note_id BIGINT NOT NULL,
    damage              TEXT,
    PRIMARY KEY (reservation_note_id, damage),
    CONSTRAINT fk_reservation_note_damage FOREIGN KEY (reservation_note_id) REFERENCES reservation_note (id)
);
