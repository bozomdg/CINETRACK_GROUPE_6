package main;

import controlleur.ConnexionControleur;
import vue.FenetreConnexion;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            FenetreConnexion vue = new FenetreConnexion();
            new ConnexionControleur(vue);
        });
    }
}