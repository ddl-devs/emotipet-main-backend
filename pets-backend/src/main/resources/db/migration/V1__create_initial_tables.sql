CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_of_birth DATE,
    photo_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    gender VARCHAR(255),
    birthdate DATE,
    breed VARCHAR(128),
    species VARCHAR(255) NOT NULL,
    weight DECIMAL(10, 2),
    height INTEGER,
    photo_url VARCHAR(256),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE pet_analisys (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    picture TEXT NOT NULL,
    result VARCHAR(32),
    accuracy DOUBLE PRECISION,
    analysis_type VARCHAR(255) NOT NULL,
    analysis_status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pet FOREIGN KEY(pet_id) REFERENCES pets(id)
);

CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    recommendation TEXT,
    category_recommendation VARCHAR(64) NOT NULL,
    pet_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pet_recommendation FOREIGN KEY(pet_id) REFERENCES pets(id)
);
