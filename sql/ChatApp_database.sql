-- ============================================================
--  ChatApp - Script de création de la base de données
--  Université de Tours - Développer des applications communicantes
-- ============================================================

CREATE DATABASE IF NOT EXISTS ChatApp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ChatApp;

-- ============================================================
-- TABLE : users
-- Stocke les informations des utilisateurs
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,               -- mot de passe haché (SHA-256)
    status      ENUM('online','offline') DEFAULT 'offline',
    theme       VARCHAR(20)  DEFAULT 'light',        -- BONUS : thème choisi par l'utilisateur
    created_at  DATETIME     DEFAULT NOW()
);

-- ============================================================
-- TABLE : chat_groups
-- Stocke les groupes de discussion
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_groups (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_by  INT,
    created_at  DATETIME DEFAULT NOW(),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- TABLE : group_members
-- Liaison entre utilisateurs et groupes (jointure)
-- ============================================================
CREATE TABLE IF NOT EXISTS group_members (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    group_id    INT NOT NULL,
    user_id     INT NOT NULL,
    role        ENUM('admin','member') DEFAULT 'member',
    joined_at   DATETIME DEFAULT NOW(),
    FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id)       ON DELETE CASCADE,
    UNIQUE KEY uq_member (group_id, user_id)
);

-- ============================================================
-- TABLE : messages
-- Stocke tous les messages (privés et de groupe)
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT NOT NULL,
    receiver_id INT  DEFAULT NULL,   -- NULL pour les messages de groupe
    group_id    INT  DEFAULT NULL,   -- NULL pour les messages privés
    content     TEXT NOT NULL,
    timestamp   DATETIME DEFAULT NOW(),
    FOREIGN KEY (sender_id)   REFERENCES users(id)       ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id)       ON DELETE CASCADE,
    FOREIGN KEY (group_id)    REFERENCES chat_groups(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE : notifications
-- BONUS : notifications pour les nouveaux messages non lus
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    message_id    INT NOT NULL,
    user_id       INT NOT NULL,                      -- destinataire de la notif
    sender_id     INT DEFAULT NULL,                  -- expéditeur du message
    seen          BOOLEAN DEFAULT FALSE,
    group_message BOOLEAN DEFAULT FALSE,
    created_at    DATETIME DEFAULT NOW(),
    FOREIGN KEY (message_id) REFERENCES messages(id)  ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id)     ON DELETE CASCADE,
    FOREIGN KEY (sender_id)  REFERENCES users(id)     ON DELETE SET NULL
);

-- ============================================================
-- VUES utiles (requêtes avec jointure - BONUS)
-- ============================================================

-- Vue : messages privés avec noms d'expéditeur et de destinataire
CREATE OR REPLACE VIEW vue_messages_prives AS
    SELECT
        m.id,
        u_sender.username  AS expediteur,
        u_recv.username    AS destinataire,
        m.content          AS message,
        m.timestamp
    FROM messages m
    JOIN users u_sender ON m.sender_id   = u_sender.id
    JOIN users u_recv   ON m.receiver_id = u_recv.id
    WHERE m.group_id IS NULL
    ORDER BY m.timestamp;

-- Vue : messages de groupe avec nom du groupe et de l'expéditeur
CREATE OR REPLACE VIEW vue_messages_groupes AS
    SELECT
        m.id,
        cg.name            AS groupe,
        u.username         AS expediteur,
        m.content          AS message,
        m.timestamp
    FROM messages m
    JOIN users       u  ON m.sender_id = u.id
    JOIN chat_groups cg ON m.group_id  = cg.id
    WHERE m.group_id IS NOT NULL
    ORDER BY m.timestamp;

-- Vue : utilisateurs connectés (BONUS statut en ligne)
CREATE OR REPLACE VIEW vue_utilisateurs_en_ligne AS
    SELECT id, username, status, created_at
    FROM users
    WHERE status = 'online';

-- Vue : notifications non lues avec détails (jointure multiple)
CREATE OR REPLACE VIEW vue_notifications_non_lues AS
    SELECT
        n.id              AS notif_id,
        u_dest.username   AS destinataire,
        u_exp.username    AS expediteur,
        m.content         AS message,
        n.group_message,
        cg.name           AS groupe,
        n.created_at
    FROM notifications n
    JOIN users       u_dest ON n.user_id   = u_dest.id
    JOIN messages    m      ON n.message_id = m.id
    LEFT JOIN users  u_exp  ON n.sender_id  = u_exp.id
    LEFT JOIN chat_groups cg ON m.group_id  = cg.id
    WHERE n.seen = FALSE
    ORDER BY n.created_at DESC;

-- ============================================================
-- DONNÉES DE TEST (optionnel - à supprimer en production)
-- Mots de passe hashés SHA-256 de "password123"
-- ============================================================
-- Hash SHA-256 de "password123" :
-- ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f

INSERT IGNORE INTO users (username, password, status) VALUES
    ('alice',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'offline'),
    ('bob',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'offline'),
    ('charlie', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'offline');

INSERT IGNORE INTO chat_groups (name, created_by) VALUES
    ('Groupe TP', 1);

INSERT IGNORE INTO group_members (group_id, user_id, role) VALUES
    (1, 1, 'admin'),
    (1, 2, 'member'),
    (1, 3, 'member');
