package vue;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Fenêtre de connexion modernisée utilisant les composants de ModernUI.
 */
public class FenetreConnexion extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField fieldUsername;
    private JPasswordField fieldPassword;
    private JButton buttonSeConnecter;
    private JLabel labelMessage;

    public FenetreConnexion() {
        // Constructeur vide conformément au pattern MVC : 
        // La logique d'initialisation est déléguée à la méthode initialiser().
    }

    public void initialiser() {
        setTitle("Cin\u00E9Track - Authentification");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        construireInterface();
        setVisible(true);
    }

    private void construireInterface() {
        // Panneau principal avec fond clair
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernUI.BG_LIGHT);

        // En-tête stylisé avec le titre de l'application
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(ModernUI.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(400, 150));

        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.gridx = 0;

        JLabel titleLabel = new JLabel("Cin\u00E9Track");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, gbcHeader);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Corps du formulaire (Card) ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel loginCard = new ModernUI.RoundedPanel(20, Color.WHITE);
        loginCard.setPreferredSize(new Dimension(320, 280));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Grille de mise en forme pour aligner les champs verticalement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Username
        JLabel userLabel = new JLabel("Nom d'utilisateur");
        userLabel.setFont(ModernUI.FONT_SMALL);
        userLabel.setForeground(ModernUI.TEXT_MUTED);
        gbc.gridy = 0;
        loginCard.add(userLabel, gbc);

        fieldUsername = ModernUI.createTextField(15);
        gbc.gridy = 1;
        loginCard.add(fieldUsername, gbc);

        // Password
        JLabel passLabel = new JLabel("Mot de passe");
        passLabel.setFont(ModernUI.FONT_SMALL);
        passLabel.setForeground(ModernUI.TEXT_MUTED);
        gbc.gridy = 2;
        loginCard.add(passLabel, gbc);

        fieldPassword = ModernUI.createPasswordField(15);
        gbc.gridy = 3;
        loginCard.add(fieldPassword, gbc);

        // Bouton de connexion
        buttonSeConnecter = ModernUI.createButton("SE CONNECTER", ModernUI.PRIMARY);
        buttonSeConnecter.setPreferredSize(new Dimension(0, 45));
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 0, 10, 0);
        loginCard.add(buttonSeConnecter, gbc);

        centerWrapper.add(loginCard);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // --- Footer / Message d'erreur ---
        labelMessage = new JLabel(" ", SwingConstants.CENTER);
        labelMessage.setFont(ModernUI.FONT_SMALL);
        labelMessage.setForeground(ModernUI.DANGER);
        labelMessage.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(labelMessage, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public void afficherErreur(String message) {
        labelMessage.setText(message);
    }

    /**
     * Expose les composants à manipuler par le contrôleur.
     * Respecte l'encapsulation tout en permettant l'injection de comportement.
     */
    public JTextField getFieldUsername() {
        return fieldUsername;
    }

    public JPasswordField getFieldPassword() {
        return fieldPassword;
    }

    public JButton getButtonSeConnecter() {
        return buttonSeConnecter;
    }
}