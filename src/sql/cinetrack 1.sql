
DROP TABLE IF EXISTS log          CASCADE;
DROP TABLE IF EXISTS reservation  CASCADE;
DROP TABLE IF EXISTS seance       CASCADE;
DROP TABLE IF EXISTS tarif        CASCADE;
DROP TABLE IF EXISTS client       CASCADE;
DROP TABLE IF EXISTS utilisateur  CASCADE;
DROP TABLE IF EXISTS salle        CASCADE;
DROP TABLE IF EXISTS film         CASCADE;
DROP TYPE  IF EXISTS role_user;
DROP TYPE  IF EXISTS categorie_tarif;

-- Pour chiffrer les mots de passe (bcrypt)
CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TYPE role_user       AS ENUM ('ADMINISTRATEUR', 'CAISSIER');
CREATE TYPE categorie_tarif AS ENUM ('NORMAL', 'ETUDIANT', 'ENFANT', 'SENIOR');


CREATE TABLE film (
    id_film   SERIAL       PRIMARY KEY,
    titre     VARCHAR(150) NOT NULL,
    genre     VARCHAR(50)  NOT NULL,
    duree     INT          NOT NULL CHECK (duree > 0),     
    synopsis  TEXT
);

CREATE TABLE salle (
    id_salle        SERIAL      PRIMARY KEY,
    nom_salle       VARCHAR(50) NOT NULL UNIQUE,
    capacite_salle  INT         NOT NULL CHECK (capacite_salle > 0)
);

CREATE TABLE seance (
    id_seance    SERIAL PRIMARY KEY,
    date_seance  DATE   NOT NULL,
    heure_debut  TIME   NOT NULL,
    place_dispo  INT    NOT NULL CHECK (place_dispo >= 0),
    id_film      INT    NOT NULL REFERENCES film(id_film)   ON DELETE RESTRICT,
    id_salle     INT    NOT NULL REFERENCES salle(id_salle) ON DELETE RESTRICT,
    -- une salle ne peut pas avoir 2 séances au même moment
    CONSTRAINT uq_salle_horaire UNIQUE (id_salle, date_seance, heure_debut)
);

CREATE TABLE client (
    id_client   SERIAL       PRIMARY KEY,
    nom_client  VARCHAR(80)  NOT NULL,
    prenom      VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) UNIQUE,
    telephone   VARCHAR(20)
);

CREATE TABLE tarif (
    id_tarif   SERIAL          PRIMARY KEY,
    prix       NUMERIC(8,2)    NOT NULL CHECK (prix >= 0),
    categorie  categorie_tarif NOT NULL UNIQUE
);

CREATE TABLE reservation (
    id_reservation  SERIAL      PRIMARY KEY,
    nb_place        INT         NOT NULL CHECK (nb_place > 0),
    statut          VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                    CHECK (statut IN ('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE')),
    id_client       INT NOT NULL REFERENCES client(id_client) ON DELETE RESTRICT,
    id_seance       INT NOT NULL REFERENCES seance(id_seance) ON DELETE RESTRICT,
    id_tarif        INT NOT NULL REFERENCES tarif(id_tarif)   ON DELETE RESTRICT
);

CREATE TABLE utilisateur (
    id_user       SERIAL       PRIMARY KEY,
    email         VARCHAR(120) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(100) NOT NULL,        
    role          role_user    NOT NULL
);

CREATE TABLE log (
    id_log             SERIAL       PRIMARY KEY,
    action             VARCHAR(200) NOT NULL,
    date_modification  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_user            INT NOT NULL REFERENCES utilisateur(id_user) ON DELETE CASCADE
);

-- Index utiles pour les recherches fréquentes
CREATE INDEX idx_seance_date        ON seance(date_seance);
CREATE INDEX idx_reservation_seance ON reservation(id_seance);
CREATE INDEX idx_reservation_client ON reservation(id_client);



--Donnee de test
INSERT INTO film (titre, genre, duree, synopsis) VALUES
('Inception',          'Science-fiction', 148, 'Un voleur s''infiltre dans les rêves pour voler des secrets.'),
('Le Roi Lion',        'Animation',        88, 'Un jeune lion doit reprendre sa place de roi.'),
('Intouchables',       'Comédie',         112, 'L''amitié improbable entre un aristocrate et son aide à domicile.'),
('Avatar',             'Science-fiction', 162, 'Un ancien marine découvre le monde de Pandora.'),
('Les Évadés',         'Drame',           142, 'Deux prisonniers nouent une amitié à travers les années.');

-- Salles
INSERT INTO salle (nom_salle, capacite_salle) VALUES
('Salle 1', 150),
('Salle 2', 100),
('Salle 3',  80),
('Salle VIP', 40);

-- Tarifs (prix en Rs)
INSERT INTO tarif (prix, categorie) VALUES
(350.00, 'NORMAL'),
(250.00, 'ETUDIANT'),
(200.00, 'ENFANT'),
(250.00, 'SENIOR');

-- Séances 
INSERT INTO seance (date_seance, heure_debut, place_dispo, id_film, id_salle) VALUES
('2026-11-10', '14:00', 144, 1, 1),   
('2026-11-10', '18:00',  97, 2, 2),   
('2026-11-11', '20:00',  80, 3, 3),   
('2026-11-12', '16:00',  38, 1, 4),  
('2026-11-12', '20:30', 150, 4, 1),  
('2026-11-13', '15:00', 100, 5, 2);   

-- Clients
INSERT INTO client (nom_client, prenom, email, telephone) VALUES
('Ramgoolam', 'Anil',    'anil.ramgoolam@mail.com', '57001122'),
('Dupont',    'Sophie',  'sophie.dupont@mail.com',  '57003344'),
('Beeharry',  'Kevin',   'kevin.beeharry@mail.com', '57005566'),
('Li',        'Mei',     'mei.li@mail.com',         '57007788'),
('Joomun',    'Priya',   'priya.joomun@mail.com',   '57009900');

-- Réservations
INSERT INTO reservation (nb_place, statut, id_client, id_seance, id_tarif) VALUES
(4, 'CONFIRMEE', 1, 1, 1),   
(2, 'CONFIRMEE', 2, 1, 2),   
(3, 'CONFIRMEE', 3, 2, 3),   
(5, 'ANNULEE',   4, 3, 1),   
(2, 'CONFIRMEE', 5, 4, 4);   


INSERT INTO utilisateur (email, mot_de_passe, role) VALUES
('admin@cinetrack.com',    crypt('admin123',    gen_salt('bf')), 'ADMINISTRATEUR'),
('caissier@cinetrack.com', crypt('caissier123', gen_salt('bf')), 'CAISSIER'),
('caissier2@cinetrack.com',crypt('caissier123', gen_salt('bf')), 'CAISSIER');

-- Logs
INSERT INTO log (action, id_user) VALUES
('Connexion administrateur',        1),
('Ajout du film Avatar',            1),
('Réservation 1 confirmée',         2),
('Annulation de la réservation 4',  2);



-- 1) Planning des séances
SELECT s.id_seance, f.titre, sa.nom_salle, s.date_seance, s.heure_debut, s.place_dispo
FROM seance s
JOIN film  f  ON f.id_film   = s.id_film
JOIN salle sa ON sa.id_salle = s.id_salle
ORDER BY s.date_seance, s.heure_debut;

-- 2) Réservations avec montant total
SELECT r.id_reservation, c.nom_client, c.prenom, f.titre,
       r.nb_place, t.categorie, r.nb_place * t.prix AS montant, r.statut
FROM reservation r
JOIN client c ON c.id_client = r.id_client
JOIN seance s ON s.id_seance = r.id_seance
JOIN film   f ON f.id_film   = s.id_film
JOIN tarif  t ON t.id_tarif  = r.id_tarif
ORDER BY r.id_reservation;

-- 3) Vérification de l'authentification 
SELECT id_user, email, role
FROM utilisateur
WHERE email = 'admin@cinetrack.com'
  AND mot_de_passe = crypt('admin123', mot_de_passe);

-- 4) Statistiques : recettes par film (réservations confirmées)
SELECT f.titre, SUM(r.nb_place) AS billets, SUM(r.nb_place * t.prix) AS recette
FROM reservation r
JOIN seance s ON s.id_seance = r.id_seance
JOIN film   f ON f.id_film   = s.id_film
JOIN tarif  t ON t.id_tarif  = r.id_tarif
WHERE r.statut = 'CONFIRMEE'
GROUP BY f.titre
ORDER BY recette DESC;
