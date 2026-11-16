package vue;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modele.Compte;

public class FenetrePrincipale extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel panelCentral;
    private JLabel labelStatut;
    private JPanel sidebar;
    private transient Compte compteConnecte;
    private JButton activeButton;
    private JButton btnFilms, btnSeances, btnGestionnaires, btnQuitter;
    private JLabel userLabel;

    private static final Color SIDEBAR_BG = new Color(33, 37, 41);
    private static final Color SIDEBAR_HOVER = new Color(52, 58, 64);
    private static final Color SIDEBAR_ACTIVE = new Color(220, 20, 60);
    private static final Color HEADER_BG = Color.WHITE;

    public FenetrePrincipale() {
        // Constructeur vide pour respecter MVC
    }

    public void setCompteConnecte(Compte compte) {
        this.compteConnecte = compte;
    }

    public void initialiser() {
        setTitle("Cin\u00E9Track");
        setSize(1300, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        construireInterface();
        setVisible(true);
    }

    private void construireInterface() {
        setLayout(new BorderLayout());

        add(creerHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(creerSidebar());
        split.setRightComponent(creerContenu());
        split.setDividerSize(0);
        split.setResizeWeight(0);
        split.setEnabled(false);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

    // Affichage de la page d'accueil par défaut au démarrage
    // Crée la barre de statut avant d'afficher l'accueil pour que labelStatut ne soit pas null
    add(creerStatusBar(), BorderLayout.SOUTH);
    afficherAccueil();
}

    private JPanel creerHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                new EmptyBorder(12, 20, 12, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel appName = new JLabel("Cin\u00E9Track");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(ModernUI.PRIMARY);

        left.add(appName);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        userLabel = new JLabel("Session : " + (compteConnecte != null ? compteConnecte.getUsername() : ""));
        userLabel.setFont(ModernUI.FONT_BODY);
        userLabel.setForeground(ModernUI.TEXT_MUTED);

        JLabel roleLabel = new JLabel("[" + (compteConnecte != null ? compteConnecte.getRole() : "") + "]");
        roleLabel.setFont(ModernUI.FONT_SMALL);
        roleLabel.setForeground(ModernUI.PRIMARY);

        JButton btnLogout = new JButton("Se d\u00e9connecter");
        btnLogout.setFont(ModernUI.FONT_SMALL);
        btnLogout.setForeground(ModernUI.PRIMARY);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            dispose();
            FenetreConnexion vc = new FenetreConnexion();
            new controlleur.ConnexionControleur(vc);
        });

        right.add(userLabel);
        right.add(roleLabel);
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel creerSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Section Navigation (Menu latéral)
        JLabel navTitle = new JLabel("NAVIGATION");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navTitle.setForeground(new Color(150, 150, 150));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTitle.setBorder(new EmptyBorder(0, 20, 15, 0));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navTitle);

        btnFilms = creerNavButton("Films", 0);
        sidebar.add(btnFilms);
        sidebar.add(Box.createVerticalStrut(5));
        
        btnSeances = creerNavButton("S\u00E9ances", 1);
        sidebar.add(btnSeances);
        sidebar.add(Box.createVerticalStrut(5));

        if (compteConnecte != null && "admin".equals(compteConnecte.getRole())) {
            btnGestionnaires = creerNavButton("Gestionnaires", 2);
            sidebar.add(btnGestionnaires);
        }

        sidebar.add(Box.createVerticalGlue());
        btnQuitter = creerNavButton("Quitter", -1);
        sidebar.add(btnQuitter);

        return sidebar;
    }

    private JButton creerNavButton(String text, int index) {
        JButton btn = new JButton(text);
        btn.setFont(ModernUI.FONT_BODY);
        btn.setForeground(new Color(200, 200, 200));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBorder(new EmptyBorder(0, 22, 0, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.setBackground(index == -1 ? new Color(40, 40, 40) : SIDEBAR_BG);
        return btn;
    }

    /**
     * Gère l'apparence du bouton actif dans le menu latéral.
     */
    public void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setForeground(new Color(200, 200, 200));
        }
        activeButton = btn;
        activeButton.setBackground(SIDEBAR_ACTIVE);
        activeButton.setForeground(Color.WHITE);
    }

    private JPanel creerContenu() {
        panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(ModernUI.BG_LIGHT);
        return panelCentral;
    }

    private JPanel creerStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(248, 249, 250));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernUI.BORDER),
                new EmptyBorder(5, 15, 5, 15)));

        labelStatut = new JLabel("Pr\u00eat.");
        labelStatut.setFont(ModernUI.FONT_SMALL);
        labelStatut.setForeground(ModernUI.TEXT_MUTED);

        JLabel version = new JLabel("v1.0");
        version.setFont(ModernUI.FONT_SMALL);
        version.setForeground(ModernUI.TEXT_MUTED);

        bar.add(labelStatut, BorderLayout.WEST);
        bar.add(version, BorderLayout.EAST);
        return bar;
    }

    public void setAccueilActions(Runnable filmsAction, Runnable seancesAction) {
        this.filmsAction = filmsAction;
        this.seancesAction = seancesAction;
        afficherAccueil();
    }

    private transient Runnable filmsAction;
    private transient Runnable seancesAction;

    /**
     * Construit dynamiquement la page d'accueil avec des cartes de statistiques/actions.
     */
    public void afficherAccueil() {
        panelCentral.removeAll();

        JPanel accueil = new JPanel(new GridBagLayout());
        accueil.setBackground(ModernUI.BG_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        JPanel welcomeCard = new ModernUI.RoundedPanel(15, Color.WHITE);
        welcomeCard.setLayout(new BorderLayout());
        welcomeCard.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel welcomeText = new JLabel("<html><div style='text-align: center;'>"
                + "<h1 style='color: #dc143c; font-size: 28px;'>Bienvenue sur Cin\u00E9Track</h1>"
                + "<p style='color: #6c757d; font-size: 16px; margin-top: 10px;'>"
                + "Utilisez le menu lat\u00E9ral pour naviguer entre les diff\u00E9rentes sections.</p>"
                + "</div></html>", SwingConstants.CENTER);
        welcomeCard.add(welcomeText, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        accueil.add(welcomeCard, gbc);

        JPanel statFilms = creerStatistique("Films", "\uD83C\uDFAC", ModernUI.ACCENT, "G\u00E9rez les films", filmsAction);
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.weighty = 0.3;
        accueil.add(statFilms, gbc);

        JPanel statSeances = creerStatistique("S\u00E9ances", "\uD83D\uDCC5", ModernUI.WARNING, "Planifiez les s\u00E9ances", seancesAction);
        gbc.gridx = 1;
        accueil.add(statSeances, gbc);

        panelCentral.add(accueil, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        setStatut("Page d'accueil");
    }

    private JPanel creerStatistique(String titre, String icone, Color couleur, String desc, Runnable action) {
        JPanel card = new ModernUI.RoundedPanel(12, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (action != null) action.run();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(252, 252, 252));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel iconLabel = new JLabel(icone);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        JLabel titleLabel = new JLabel(titre);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ModernUI.TEXT_DARK);
        top.add(iconLabel, BorderLayout.WEST);
        top.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(ModernUI.FONT_BODY);
        descLabel.setForeground(ModernUI.TEXT_MUTED);
        descLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(top, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.SOUTH);
        return card;
    }

    public void afficherFilms() {
        panelCentral.removeAll();
        FenetreFilms ff = new FenetreFilms();
        new controlleur.FilmControleur(ff);
        panelCentral.add(ff, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        setStatut("Gestion des films");
    }

    public void afficherSeances() {
        panelCentral.removeAll();
        FenetreSeances fs = new FenetreSeances();
        new controlleur.SeanceControleur(fs, this);
        panelCentral.add(fs, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        setStatut("Gestion des s\u00E9ances");
    }

    public void afficherGestion() {
        panelCentral.removeAll();
        FenetreGestionComptes fg = new FenetreGestionComptes();
        new controlleur.GestionComptesControleur(fg);
        panelCentral.add(fg, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        setStatut("Gestion des comptes");
    }

    public void setStatut(String message) {
        labelStatut.setText("  " + message);
    }

    public JButton getBtnFilms() { return btnFilms; }
    public JButton getBtnSeances() { return btnSeances; }
    public JButton getBtnGestionnaires() { return btnGestionnaires; }
    public JButton getBtnQuitter() { return btnQuitter; }
    public Compte getCompteConnecte() { return compteConnecte; }
}