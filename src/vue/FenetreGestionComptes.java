package vue;

import controlleur.GestionComptesControleur;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Vue modernisée pour gérer les gestionnaires avec ModernUI.
 * Respecte le pattern MVC (pas de logique dans la vue).
 */
public class FenetreGestionComptes extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable table;
    private DefaultTableModel modeleTable;
    private transient GestionComptesControleur controleur;

    private JTextField champUsername;
    private JPasswordField champPassword;
    private JButton btnAjouter, btnSupprimer, btnRafraichir, btnLogs;

    public FenetreGestionComptes() {
        // Constructeur vide pour respecter MVC
    }

    public void initialiser(GestionComptesControleur controleur) {
        this.controleur = controleur;
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernUI.BG_LIGHT);

        construireInterface();
    }

    private void construireInterface() {
        // --- Toolbar ---
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernUI.BORDER),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel title = new JLabel("Gestion des Comptes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ModernUI.TEXT_DARK);
        toolbar.add(title, BorderLayout.WEST);

        // Boutons d'action dans la toolbar
        JPanel actionsToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsToolbar.setOpaque(false);

        btnRafraichir = ModernUI.createButton("Rafra\u00EEchir", ModernUI.ACCENT);
        btnLogs = ModernUI.createButton("Voir Logs", new Color(108, 117, 125));
        
        Dimension tDim = new Dimension(130, 36);
        btnRafraichir.setPreferredSize(tDim);
        btnLogs.setPreferredSize(tDim);

        btnRafraichir.addActionListener(e -> controleur.chargerDonnees());
        btnLogs.addActionListener(e -> controleur.afficherLogs());

        actionsToolbar.add(btnRafraichir);
        actionsToolbar.add(btnLogs);
        toolbar.add(actionsToolbar, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // --- Table ---
        String[] colonnes = {"ID", "Nom d'utilisateur"};
        modeleTable = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(modeleTable);
        ModernUI.styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Formulaire (Côté Droit) ---
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setOpaque(false);
        eastPanel.setBorder(new EmptyBorder(20, 0, 20, 20));
        eastPanel.setPreferredSize(new Dimension(320, 0));

        JPanel formCard = ModernUI.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JLabel formTitle = new JLabel("Nouveau Compte");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridy = 0;
        formCard.add(formTitle, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(15, 5, 2, 5);
        formCard.add(new JLabel("Username"), gbc);
        champUsername = ModernUI.createTextField(15);
        gbc.gridy = 2; gbc.insets = new Insets(0, 5, 10, 5);
        formCard.add(champUsername, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(5, 5, 2, 5);
        formCard.add(new JLabel("Mot de passe"), gbc);
        champPassword = ModernUI.createPasswordField(15);
        gbc.gridy = 4; gbc.insets = new Insets(0, 5, 10, 5);
        formCard.add(champPassword, gbc);

        eastPanel.add(formCard, BorderLayout.CENTER);

        // --- Boutons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        btnAjouter = ModernUI.createButton("Ajouter", ModernUI.SUCCESS);
        btnSupprimer = ModernUI.createButton("Supprimer", ModernUI.DANGER);

        Dimension bDim = new Dimension(130, 40);
        btnAjouter.setPreferredSize(bDim);
        btnSupprimer.setPreferredSize(bDim);

        btnAjouter.addActionListener(e -> controleur.actionAjouter(champUsername.getText(), new String(champPassword.getPassword())));
        btnSupprimer.addActionListener(e -> controleur.actionSupprimer());

        btnPanel.add(btnAjouter);
        btnPanel.add(btnSupprimer);

        eastPanel.add(btnPanel, BorderLayout.SOUTH);
        add(eastPanel, BorderLayout.EAST);
    }

    public void viderChamps() {
        champUsername.setText("");
        champPassword.setText("");
    }

    public void afficherSucces(String msg) {
        ModernUI.showSuccess(this, msg);
    }

    public void afficherErreur(String msg) {
        ModernUI.showError(this, msg);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getModeleTable() { return modeleTable; }
}