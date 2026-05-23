CREATE TABLE storageBin (
    id BIGSERIAL PRIMARY KEY,
    latitude FLOAT NOT NULL ,
    longitude FLOAT NOT NULL
);


CREATE TABLE forklift (
    id BIGSERIAL PRIMARY KEY,
    weight_capacity INTEGER NOT NULL CHECK (weight_capacity > 0),
    equipment_type VARCHAR(50) NOT NULL,
    current_location_id BIGINT,
    CONSTRAINT fk_forklift_location FOREIGN KEY (current_location_id) REFERENCES storageBin (id)
);


CREATE TABLE transportOrder (
    id BIGSERIAL PRIMARY KEY,
    pick_location_id BIGINT NOT NULL,
    delivery_location_id BIGINT NOT NULL,
    weight INTEGER NOT NULL CHECK (weight > 0),
    status VARCHAR(50) NOT NULL,
    required_equipment VARCHAR(50) NOT NULL,
    forklift_id BIGINT,
    CONSTRAINT fk_task_pick_location FOREIGN KEY (pick_location_id) REFERENCES storageBin (id),
    CONSTRAINT fk_task_delivery_location FOREIGN KEY (delivery_location_id) REFERENCES storageBin (id),
    CONSTRAINT fk_task_forklift FOREIGN KEY (forklift_id) REFERENCES forklift (id)
);


CREATE INDEX idx_task_forklift ON transportOrder(forklift_id);
CREATE INDEX idx_forklift_location ON forklift(current_location_id);