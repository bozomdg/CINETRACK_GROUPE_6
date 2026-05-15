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

    public void afficherLogs() {
        modele.LogGestionnaireDAO logDAO = new modele.LogGestionnaireDAO();
        List<modele.LogGestionnaire> logs = logDAO.findAll();

        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(vue,
                    "Aucun log d'activité disponible.",
                    "Logs des Gestionnaires",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = {"ID Log", "Action", "Nom d'utilisateur", "ID Gestionnaire", "Date"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (modele.LogGestionnaire log : logs) {
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
