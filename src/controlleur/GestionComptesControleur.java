package controlleur;

import exception.ValidationException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modele.Compte;
import modele.LogGestionnaire;
import service.CompteService;
import vue.FenetreGestionComptes;
import vue.ModernUI;

/**
 * Contrôleur pour la gestion des comptes et la consultation des logs.
 * Délègue les actions et validations à CompteService.
 */
@SuppressWarnings("this-escape")
public class GestionComptesControleur {

    private final FenetreGestionComptes vue;
    private final CompteService compteService;

    public GestionComptesControleur(FenetreGestionComptes vue) {
        this.vue = vue;
        this.compteService = new CompteService();
        this.vue.initialiser(this);
        chargerDonnees();
    }

    public GestionComptesControleur(FenetreGestionComptes vue, CompteService compteService) {
        this.vue = vue;
        this.compteService = compteService;
        this.vue.initialiser(this);
        chargerDonnees();
    }

    public void chargerDonnees() {
        vue.getModeleTable().setRowCount(0);
        List<Compte> gestionnaires = compteService.getTousLesGestionnaires();
        for (Compte c : gestionnaires) {
            vue.getModeleTable().addRow(new Object[]{c.getId(), c.getUsername()});
        }
    }

    public void actionAjouter(String user, String pass) {
        try {
            compteService.ajouterGestionnaire(user, pass);
            vue.afficherSucces("Gestionnaire ajouté avec succès !");
            vue.viderChamps();
            chargerDonnees();
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    public void actionSupprimer() {
        int ligne = vue.getTable().getSelectedRow();
        if (ligne < 0) {
            vue.afficherErreur("Veuillez sélectionner un gestionnaire.");
            return;
        }

        int id = (int) vue.getModeleTable().getValueAt(ligne, 0);
        if (ModernUI.showConfirm(vue, "Supprimer ce gestionnaire ?") == JOptionPane.YES_OPTION) {
            try {
                compteService.supprimerGestionnaire(id);
                vue.afficherSucces("Gestionnaire supprimé.");
                chargerDonnees();
            } catch (ValidationException e) {
                vue.afficherErreur(e.getMessage());
            }
        }
    }

    public void afficherLogs() {
        List<LogGestionnaire> logs = compteService.getLogsActivite();

        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                    "Aucun log d'activité disponible.",
                    "Logs des Gestionnaires",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = {"ID Log", "Action", "Nom d'utilisateur", "ID Gestionnaire", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (LogGestionnaire log : logs) {
            model.addRow(new Object[]{
                    log.getIdLog(),
                    log.getAction(),
                    log.getUsernameGestionnaire(),
                    log.getIdGestionnaire(),
                    log.getDateAction() != null ? log.getDateAction().toString().substring(0, 19) : ""
            });
        }

        JTable tableLogs = new JTable(model);
        ModernUI.styleTable(tableLogs);
        JScrollPane scroll = new JScrollPane(tableLogs);
        scroll.setPreferredSize(new java.awt.Dimension(600, 300));

        JOptionPane.showMessageDialog(vue, scroll,
                "Historique des actions sur les gestionnaires",
                JOptionPane.PLAIN_MESSAGE);
    }
}
