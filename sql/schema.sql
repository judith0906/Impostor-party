CREATE DATABASE IF NOT EXISTS impostor_party
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE impostor_party;
SET NAMES utf8mb4;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(6) UNIQUE NOT NULL,
    host_id INT NOT NULL,
    num_impostors INT NOT NULL DEFAULT 1,
    challenge_type VARCHAR(20) NOT NULL,
    end_time TIMESTAMP NULL,
    duration_hours INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LOBBY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (host_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    room_id INT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    session_token VARCHAR(64) NOT NULL,
    is_impostor BOOLEAN NULL,
    ready BOOLEAN NOT NULL DEFAULT FALSE,
    word_assigned VARCHAR(100) NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_players_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uq_players_room_user (room_id, user_id),
    UNIQUE KEY uq_players_session_token (session_token),
    UNIQUE KEY unique_nick_per_room (room_id, nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE words (
    id INT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    challenge_type VARCHAR(20) NOT NULL,
    UNIQUE KEY uq_words_challenge_word (challenge_type, word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    challenge_type VARCHAR(20) NOT NULL,
    is_for_impostor BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_tasks_challenge_description (challenge_type, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    task_id INT NOT NULL,
    position INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
