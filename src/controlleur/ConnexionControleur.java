package controlleur;

import exception.ValidationException;
import modele.Compte;
import service.CompteService;
import vue.FenetreConnexion;
import vue.FenetrePrincipale;

/**
 * Contrôleur pour la fenêtre de connexion.
 * Reçoit les événements de la vue et appelle la couche Service.
 */
public class ConnexionControleur {

    private final FenetreConnexion vue;
    private final CompteService compteService;

    public ConnexionControleur(FenetreConnexion vue) {
        this.vue = vue;
        this.compteService = new CompteService();

        // Initialisation de la vue (Layout et composants)
        this.vue.initialiser();

        // Ajout de l'écouteur sur le bouton de connexion
        this.vue.getButtonSeConnecter().addActionListener(e -> actionSeConnecter());
    }

    public ConnexionControleur(FenetreConnexion vue, CompteService compteService) {
        this.vue = vue;
        this.compteService = compteService;
        this.vue.initialiser();
        this.vue.getButtonSeConnecter().addActionListener(e -> actionSeConnecter());
    }

    private void actionSeConnecter() {
        String username = vue.getFieldUsername().getText();
        String password = new String(vue.getFieldPassword().getPassword());

        try {
            // Appel à la couche Service (validation + DAO)
            Compte compte = compteService.authentifier(username, password);

            // Authentification réussie : passage à la fenêtre principale
            vue.dispose();
            FenetrePrincipale vuePrincipale = new FenetrePrincipale();
            new PrincipaleControleur(vuePrincipale, compte);

        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }
}
