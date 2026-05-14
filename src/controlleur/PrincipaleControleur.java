package controlleur;

import modele.Compte;
import vue.FenetrePrincipale;

import javax.swing.*;

/**
 * Contrôleur pour la fenêtre principale.
 * Gère la navigation et l'affichage des différents modules.
 */
public class PrincipaleControleur {

    private FenetrePrincipale vue;
    public PrincipaleControleur(FenetrePrincipale vue, Compte compte) {
        this.vue = vue;

        // On passe le compte à la vue et on l'initialise
        this.vue.setCompteConnecte(compte);
        this.vue.initialiser();

        // Ajout des écouteurs pour la navigation latérale
        this.vue.getBtnFilms().addActionListener(e -> {
            this.vue.setActiveButton((JButton) e.getSource());
            this.vue.afficherFilms();
        });

        this.vue.getBtnSeances().addActionListener(e -> {
            this.vue.setActiveButton((JButton) e.getSource());
            this.vue.afficherSeances();
        });

        if (this.vue.getBtnGestionnaires() != null) {
            this.vue.getBtnGestionnaires().addActionListener(e -> {
                this.vue.setActiveButton((JButton) e.getSource());
                this.vue.afficherGestion();
            });
        }

        this.vue.getBtnQuitter().addActionListener(e -> System.exit(0));

        // Ajout des écouteurs pour les cartes de l'accueil
        this.vue.setAccueilActions(this.vue::afficherFilms, this.vue::afficherSeances);
    }
}
