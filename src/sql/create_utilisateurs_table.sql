-- Création de la table utilisateurs pour l'application CinéTrack
-- À exécuter dans pgAdmin ou tout autre client PostgreSQL

CREATE TABLE IF NOT EXISTS utilisateurs (
    id_utilisateur SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'gestionnaire'))
);

-- Insertion d'utilisateurs de test
-- Mot de passe : admin123 (à changer en production !)
INSERT INTO utilisateurs (username, password, role) VALUES 
('admin', 'admin123', 'admin')
ON CONFLICT (username) DO NOTHING;

-- Mot de passe : gestion123 (à changer en production !)
INSERT INTO utilisateurs (username, password, role) VALUES 
('gestionnaire', 'gestion123', 'gestionnaire')
ON CONFLICT (username) DO NOTHING;

-- Pour voir tous les utilisateurs :
-- SELECT * FROM utilisateurs;