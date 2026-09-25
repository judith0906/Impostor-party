-- FILE: sql/schema.sql
CREATE DATABASE IF NOT EXISTS impostor_party;
USE impostor_party;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(6) UNIQUE NOT NULL,
    host_id INT NOT NULL,
    num_impostors INT NOT NULL DEFAULT 1,
    challenge_type VARCHAR(20) NOT NULL, -- extremos, amigos, salseo, picantes, normales
    end_time TIMESTAMP NULL,             -- se fija cuando arranca la partida
    duration_hours INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LOBBY', -- LOBBY, IN_PROGRESS, FINISHED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (host_id) REFERENCES users(id)
);

CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    session_token VARCHAR(64) NOT NULL, -- identifica al jugador sin necesitar login
    is_impostor BOOLEAN NULL,           -- NULL hasta que arranca el juego
    ready BOOLEAN NOT NULL DEFAULT FALSE,
    word_assigned VARCHAR(100) NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    UNIQUE KEY unique_nick_per_room (room_id, nickname)
);

CREATE TABLE words (
    id INT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    challenge_type VARCHAR(20) NOT NULL
);

CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    challenge_type VARCHAR(20) NOT NULL,
    is_for_impostor BOOLEAN NOT NULL DEFAULT FALSE -- tareas específicas de impostor si quieres diferenciarlas más
);

CREATE TABLE player_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    task_id INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id)
);