package controlleur;

import modele.Compte;
import modele.CompteDAO;
import vue.FenetreGestionComptes;
import vue.ModernUI;

import javax.swing.*;
import java.util.List;

public class GestionComptesControleur {
    private FenetreGestionComptes vue;
    private CompteDAO dao;

    public GestionComptesControleur(FenetreGestionComptes vue) {
        this.vue = vue;
        this.dao = new CompteDAO();
        this.vue.initialiser(this);
        chargerDonnees();
    }

    public void chargerDonnees() {
        vue.getModeleTable().setRowCount(0);
        List<Compte> gestionnaires = dao.findAllGestionnaires();
        for (Compte c : gestionnaires) {
            vue.getModeleTable().addRow(new Object[]{c.getId(), c.getUsername()});
        }
    }

    public void actionAjouter(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            vue.afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        Compte nouveau = new Compte(0, user, pass, "gestionnaire");
        if (dao.insertGestionnaire(nouveau)) {
            vue.afficherSucces("Gestionnaire ajouté !");
            vue.viderChamps();
            chargerDonnees();
        } else {
            vue.afficherErreur("Erreur lors de l'ajout.");
        }
    }

    public void actionSupprimer() {
        int ligne = vue.getTable().getSelectedRow();
        if (ligne >= 0) {
            int id = (int) vue.getModeleTable().getValueAt(ligne, 0);
            if (ModernUI.showConfirm(vue, "Supprimer ce gestionnaire ?") == JOptionPane.YES_OPTION) {
                if (dao.deleteGestionnaire(id)) {
                    chargerDonnees();
                } else {
                    vue.afficherErreur("Erreur lors de la suppression.");
                }
            }
        } else {
            vue.afficherErreur("Veuillez sélectionner un gestionnaire.");
        }
    }
}
