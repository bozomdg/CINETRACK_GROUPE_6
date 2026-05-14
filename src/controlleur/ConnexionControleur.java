package controlleur;

import modele.Compte;
import modele.CompteDAO;
import vue.FenetreConnexion;
import vue.FenetrePrincipale;

/**
 * Contrôleur pour la fenêtre de connexion.
 * Gère l'authentification et la transition vers la fenêtre principale.
 */
public class ConnexionControleur {

    private FenetreConnexion vue;
    private CompteDAO dao;

    public ConnexionControleur(FenetreConnexion vue) {
        this.vue = vue;
        this.dao = new CompteDAO();

        // Initialisation de la vue (Layout et composants)
        this.vue.initialiser();

        // Ajout de l'écouteur sur le bouton de connexion
        this.vue.getButtonSeConnecter().addActionListener(e -> actionSeConnecter());
    }

    private void actionSeConnecter() {
        String username = vue.getFieldUsername().getText().trim();
        String password = new String(vue.getFieldPassword().getPassword());

        // Validation simple des champs
        if (username.isEmpty() || password.isEmpty()) {
            vue.afficherErreur("Champs obligatoires.");
            return;
        }

        // Vérification des identifiants via le Model (DAO)
        Compte compte = dao.findByUsernameAndPassword(username, password);

        if (compte != null) {
            // Authentification réussie : on ferme la connexion et on lance l'application
            vue.dispose();
            FenetrePrincipale vuePrincipale = new FenetrePrincipale();
            new PrincipaleControleur(vuePrincipale, compte);
        } else {
            // Échec : retour visuel à l'utilisateur
            vue.afficherErreur("Identifiants incorrects.");
        }
    }
}
