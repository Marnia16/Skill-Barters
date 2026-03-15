-- ============================================================
-- AWS RDS MySQL Setup Script for Skill Barter System
-- Run this in MySQL Workbench or any SQL client after
-- connecting to your AWS RDS endpoint
-- ============================================================

CREATE DATABASE IF NOT EXISTS skillbarter_db;
USE skillbarter_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    user_type   ENUM('REGISTERED', 'GUEST') DEFAULT 'REGISTERED',
    bio         TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Skills table
CREATE TABLE IF NOT EXISTS skills (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  TEXT,
    category     VARCHAR(50),
    skill_level  ENUM('BEGINNER', 'INTERMEDIATE', 'EXPERT') DEFAULT 'BEGINNER',
    is_available BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Barter Requests table
CREATE TABLE IF NOT EXISTS barter_requests (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    requester_id     INT NOT NULL,
    provider_id      INT NOT NULL,
    offered_skill_id INT NOT NULL,
    wanted_skill_id  INT NOT NULL,
    status           ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    message          TEXT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id)     REFERENCES users(id),
    FOREIGN KEY (provider_id)      REFERENCES users(id),
    FOREIGN KEY (offered_skill_id) REFERENCES skills(id),
    FOREIGN KEY (wanted_skill_id)  REFERENCES skills(id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    message    VARCHAR(255) NOT NULL,
    is_read    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sample data
INSERT INTO users (name, email, password, user_type, bio) VALUES
('Alice Johnson', 'alice@example.com', 'hashed_password_1', 'REGISTERED', 'Web developer, loves teaching React'),
('Bob Smith',    'bob@example.com',   'hashed_password_2', 'REGISTERED', 'Graphic designer and photographer');

INSERT INTO skills (user_id, title, description, category, skill_level) VALUES
(1, 'React JS',        'Frontend development with React hooks', 'Programming',  'EXPERT'),
(1, 'Python Basics',   'Beginner Python scripting',             'Programming',  'INTERMEDIATE'),
(2, 'Logo Design',     'Professional logo and brand design',    'Design',       'EXPERT'),
(2, 'Photography',     'Portrait and landscape photography',    'Photography',  'INTERMEDIATE');
