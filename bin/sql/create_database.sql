-- Script SQL mis à jour : Séparation Admins et Gestionnaires
-- Suppression du concept générique "Utilisateurs"

-- =============================================
-- TABLE ADMINS
-- =============================================
DROP TABLE IF EXISTS admins CASCADE;
CREATE TABLE admins (
    id_admin SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- =============================================
-- TABLE GESTIONNAIRES
-- =============================================
DROP TABLE IF EXISTS gestionnaires CASCADE;
CREATE TABLE gestionnaires (
    id_gestionnaire SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- =============================================
-- TABLES MÉTIER (Inchangées mais liées)
-- =============================================
DROP TABLE IF EXISTS films CASCADE;
CREATE TABLE films (
    id_film SERIAL PRIMARY KEY,
    titre VARCHAR(100) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    duree INTEGER NOT NULL CHECK (duree > 0),
    note DECIMAL(2,1),
    synopsis TEXT
);

DROP TABLE IF EXISTS salles CASCADE;
CREATE TABLE salles (
    id_salle SERIAL PRIMARY KEY,
    nom_salle VARCHAR(50) NOT NULL,
    capacite INTEGER NOT NULL,
    est_3d BOOLEAN DEFAULT FALSE
);

DROP TABLE IF EXISTS seances CASCADE;
CREATE TABLE seances (
    id_seance SERIAL PRIMARY KEY,
    id_film INTEGER NOT NULL REFERENCES films(id_film) ON DELETE CASCADE,
    id_salle INTEGER NOT NULL REFERENCES salles(id_salle) ON DELETE CASCADE,
    date_seance DATE NOT NULL,
    heure TIME NOT NULL,
    places_dispo INTEGER NOT NULL
);

-- =============================================
-- INSERTIONS DE TEST
-- =============================================

INSERT INTO admins (username, password) VALUES ('admin', 'admin123');
INSERT INTO gestionnaires (username, password) VALUES ('gestion', 'gestion123');

INSERT INTO films (titre, genre, duree, note) VALUES 
('Inception', 'Science-Fiction', 148, 8.8),
('Avatar', 'Action', 162, 7.9);

INSERT INTO salles (nom_salle, capacite, est_3d) VALUES 
('Salle 1', 150, FALSE),
('Salle 2', 100, TRUE);