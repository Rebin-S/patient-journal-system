-- Create tables in dependency order for FKs to work

CREATE TABLE organization
(
    id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    type    VARCHAR(100),
    address VARCHAR(255)
);

CREATE TABLE patient
(
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    personnummer VARCHAR(32) NOT NULL UNIQUE,
    name         VARCHAR(255),
    birth_date   DATE,
    gender       VARCHAR(20),
    contact_info VARCHAR(255)
);

CREATE TABLE practitioner
(
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    title           VARCHAR(100),
    organization_id BIGINT NULL,
    CONSTRAINT fk_practitioner_org
        FOREIGN KEY (organization_id) REFERENCES organization (id)
);

CREATE TABLE location
(
    id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    name    VARCHAR(255),
    address VARCHAR(255)
);

CREATE TABLE encounter
(
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    practitioner_id BIGINT NULL,
    location_id     BIGINT NULL,
    start_time      DATETIME2 NULL,
    end_time        DATETIME2 NULL,
    notes           NVARCHAR(MAX) NULL,
    CONSTRAINT fk_encounter_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_encounter_practitioner
        FOREIGN KEY (practitioner_id) REFERENCES practitioner (id),
    CONSTRAINT fk_encounter_location
        FOREIGN KEY (location_id) REFERENCES location (id)
);

CREATE TABLE [condition]
(
    id
    BIGINT
    IDENTITY
(
    1,
    1
) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    code VARCHAR
(
    50
) NOT NULL,
    display VARCHAR
(
    255
),
    onset_date DATE,
    asserted_by_practitioner_id BIGINT NULL,
    CONSTRAINT fk_cond_patient
    FOREIGN KEY
(
    patient_id
) REFERENCES patient
(
    id
),
    CONSTRAINT fk_cond_pract
    FOREIGN KEY
(
    asserted_by_practitioner_id
) REFERENCES practitioner
(
    id
)
    );

CREATE TABLE observation
(
    id                          BIGINT IDENTITY(1,1) PRIMARY KEY,
    patient_id                  BIGINT NOT NULL,
    type                        VARCHAR(100),
    value                       VARCHAR(100),
    unit                        VARCHAR(50),
    observed_at                 DATETIME2,
    recorded_by_practitioner_id BIGINT NULL,
    encounter_id                BIGINT NULL,
    CONSTRAINT fk_obs_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_obs_pract
        FOREIGN KEY (recorded_by_practitioner_id) REFERENCES practitioner (id),
    CONSTRAINT fk_obs_encounter
        FOREIGN KEY (encounter_id) REFERENCES encounter (id)
);

CREATE TABLE users
(
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    patient_id      BIGINT NULL,
    practitioner_id BIGINT NULL,
    CONSTRAINT fk_user_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_user_practitioner
        FOREIGN KEY (practitioner_id) REFERENCES practitioner (id)
);

CREATE TABLE message
(
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_user_id   BIGINT NOT NULL,
    receiver_user_id BIGINT NOT NULL,
    content          NVARCHAR(MAX) NOT NULL,
    sent_at          DATETIME2 DEFAULT SYSDATETIME(),
    is_read          BIT       DEFAULT 0,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_user_id) REFERENCES users (id),
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_user_id) REFERENCES users (id)
);
