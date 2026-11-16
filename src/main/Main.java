package main;

import controlleur.ConnexionControleur;
import javax.swing.UIManager;
import vue.FenetreConnexion;

public class Main {
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Impossible de changer l'apparence : " + e.getMessage());
        }

        // Ouvre la fenêtre de connexion
        FenetreConnexion vue = new FenetreConnexion();
        new ConnexionControleur(vue);
    }
}