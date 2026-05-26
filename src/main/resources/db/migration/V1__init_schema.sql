CREATE TABLE storage_bin (
    id BIGSERIAL PRIMARY KEY,
    bin_code VARCHAR(255) NOT NULL UNIQUE,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    zone_type VARCHAR(100) NOT NULL,
    max_weight_capacity_kg INTEGER NOT NULL
);

CREATE TABLE forklift_types (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(255) NOT NULL UNIQUE,
    equipment_type VARCHAR(50) NOT NULL,
    max_capacity_kg INTEGER NOT NULL,
    total_battery_capacity_kwh DOUBLE PRECISION NOT NULL,
    base_energy_consumption_per_meter DOUBLE PRECISION NOT NULL
);

CREATE TABLE forklifts (
    id BIGSERIAL PRIMARY KEY,
    fleet_number VARCHAR(255) NOT NULL UNIQUE,
    forklift_type_id BIGINT NOT NULL,
    current_storage_bin_id BIGINT,
    operational_status VARCHAR(50) NOT NULL,
    current_battery_percentage DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_forklift_type FOREIGN KEY (forklift_type_id) REFERENCES forklift_types (id),
    CONSTRAINT fk_forklift_current_bin FOREIGN KEY (current_storage_bin_id) REFERENCES storage_bin (id)
);

CREATE TABLE load_units (
    id BIGSERIAL PRIMARY KEY,
    tracking_code VARCHAR(255) NOT NULL UNIQUE,
    weight_kg INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_storage_bin_id BIGINT,
    version BIGINT,
    CONSTRAINT fk_loadunit_current_bin FOREIGN KEY (current_storage_bin_id) REFERENCES storage_bin (id)
);

CREATE TABLE transport_orders (
    id BIGSERIAL PRIMARY KEY,
    load_unit_id BIGINT NOT NULL,
    target_bin_id BIGINT NOT NULL,
    source_bin_id BIGINT NOT NULL,
    required_equipment VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    forklift_id BIGINT,
    CONSTRAINT fk_transport_loadunit FOREIGN KEY (load_unit_id) REFERENCES load_units (id),
    CONSTRAINT fk_transport_target_bin FOREIGN KEY (target_bin_id) REFERENCES storage_bin (id),
    CONSTRAINT fk_transport_source_bin FOREIGN KEY (source_bin_id) REFERENCES storage_bin (id),
    CONSTRAINT fk_transport_forklift FOREIGN KEY (forklift_id) REFERENCES forklifts (id)
);

CREATE INDEX idx_transport_forklift ON transport_orders(forklift_id);
CREATE INDEX idx_forklift_current_bin ON forklifts(current_storage_bin_id);
CREATE INDEX idx_transport_loadunit ON transport_orders(load_unit_id);
