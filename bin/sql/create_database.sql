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
-- TABLE RESERVATIONS
-- Chaque réservation lie un client (nom/prénom) à une séance,
-- et enregistre quel compte (admin ou gestionnaire) l'a créée.
-- =============================================
DROP TABLE IF EXISTS reservations CASCADE;
CREATE TABLE reservations (
    id_reservation SERIAL PRIMARY KEY,
    id_seance INTEGER NOT NULL REFERENCES seances(id_seance) ON DELETE CASCADE,
    nom_client VARCHAR(100) NOT NULL,
    prenom_client VARCHAR(100) NOT NULL,
    nb_places INTEGER NOT NULL CHECK (nb_places > 0),
    date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Traçabilité : quel compte a effectué la réservation
    id_compte INTEGER NOT NULL,
    role_compte VARCHAR(20) NOT NULL CHECK (role_compte IN ('admin', 'gestionnaire'))
);

-- =============================================
-- TRIGGER : Décrémenter automatiquement les places disponibles
-- lors d'une nouvelle réservation (INSERT)
-- =============================================
CREATE OR REPLACE FUNCTION trigger_decrementer_places()
RETURNS TRIGGER AS $$
BEGIN
    -- Vérifie qu'il y a assez de places avant de décrémenter
    IF (SELECT places_dispo FROM seances WHERE id_seance = NEW.id_seance) < NEW.nb_places THEN
        RAISE EXCEPTION 'Places insuffisantes pour cette séance.';
    END IF;

    UPDATE seances
    SET places_dispo = places_dispo - NEW.nb_places
    WHERE id_seance = NEW.id_seance;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reservation_insert
AFTER INSERT ON reservations
FOR EACH ROW
EXECUTE FUNCTION trigger_decrementer_places();

-- =============================================
-- TRIGGER : Ré-incrémenter les places si une réservation est annulée (DELETE)
-- =============================================
CREATE OR REPLACE FUNCTION trigger_incrementer_places()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE seances
    SET places_dispo = places_dispo + OLD.nb_places
    WHERE id_seance = OLD.id_seance;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reservation_delete
AFTER DELETE ON reservations
FOR EACH ROW
EXECUTE FUNCTION trigger_incrementer_places();

-- =============================================
-- TABLE LOGS_GESTIONNAIRES
-- Enregistre automatiquement les ajouts et suppressions de gestionnaires.
-- =============================================
DROP TABLE IF EXISTS logs_gestionnaires CASCADE;
CREATE TABLE logs_gestionnaires (
    id_log SERIAL PRIMARY KEY,
    action VARCHAR(20) NOT NULL,         -- 'AJOUT' ou 'SUPPRESSION'
    username_gestionnaire VARCHAR(50) NOT NULL,
    id_gestionnaire INTEGER NOT NULL,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TRIGGER : Logger automatiquement l'ajout d'un gestionnaire
-- =============================================
CREATE OR REPLACE FUNCTION trigger_log_ajout_gestionnaire()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO logs_gestionnaires (action, username_gestionnaire, id_gestionnaire)
    VALUES ('AJOUT', NEW.username, NEW.id_gestionnaire);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gestionnaire_insert
AFTER INSERT ON gestionnaires
FOR EACH ROW
EXECUTE FUNCTION trigger_log_ajout_gestionnaire();

-- =============================================
-- TRIGGER : Logger automatiquement la suppression d'un gestionnaire
-- =============================================
CREATE OR REPLACE FUNCTION trigger_log_suppression_gestionnaire()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO logs_gestionnaires (action, username_gestionnaire, id_gestionnaire)
    VALUES ('SUPPRESSION', OLD.username, OLD.id_gestionnaire);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gestionnaire_delete
AFTER DELETE ON gestionnaires
FOR EACH ROW
EXECUTE FUNCTION trigger_log_suppression_gestionnaire();

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

INSERT INTO seances (id_film, id_salle, date_seance, heure, places_dispo) VALUES
(1, 1, '2026-06-01', '14:00', 150),
(2, 2, '2026-06-01', '20:00', 100);