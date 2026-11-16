package controlleur;

import exception.ValidationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import modele.Compte;
import service.CompteService;
import vue.FenetreConnexion;
import vue.FenetrePrincipale;

/**
 * Contrôleur de la fenêtre de connexion.
 * Reçoit le clic sur le bouton et appelle le service.
 */
public class ConnexionControleur {

    private FenetreConnexion vue;
    private CompteService compteService;

    public ConnexionControleur(FenetreConnexion vue) {
        this.vue = vue;
        this.compteService = new CompteService();

        // Prépare la fenêtre
        this.vue.initialiser();

        // Quand on clique sur "Se connecter", on appelle seConnecter()
        this.vue.getButtonSeConnecter().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seConnecter();
            }
        });
    }

    /** Vérifie l'identifiant et le mot de passe, puis ouvre la fenêtre principale */
    private void seConnecter() {
        String username = vue.getFieldUsername().getText();
        String password = new String(vue.getFieldPassword().getPassword());

        try {
            Compte compte = compteService.authentifier(username, password);

            // Connexion réussie : on ferme la connexion et on ouvre la fenêtre principale
            vue.dispose();
            FenetrePrincipale vuePrincipale = new FenetrePrincipale();
            new PrincipaleControleur(vuePrincipale, compte);

        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }
}