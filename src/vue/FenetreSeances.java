package vue;

import controlleur.SeanceControleur;
import modele.Seance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FenetreSeances extends JPanel {

    private FenetrePrincipale fenetrePrincipale;
    private SeanceControleur controleur;
    private JTable table;
    private DefaultTableModel modeleTable;
    private JTextField champDate;

    private JTextField champIdFilm, champIdSalle, champDateSeance, champHeure, champPlaces;
    private JButton btnAjouter, btnModifier, btnSupprimer;
    private JLabel countLabel;

    public FenetreSeances() {
        // Constructeur vide pour respecter MVC
    }

    public void initialiser(FenetrePrincipale parent, SeanceControleur controleur) {
        this.fenetrePrincipale = parent;
        this.controleur = controleur;
        construireInterface();
    }

    private void construireInterface() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernUI.BG_LIGHT);

        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernUI.BORDER),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Gestion des S\u00e9ances");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ModernUI.TEXT_DARK);
        countLabel = new JLabel();
        countLabel.setFont(ModernUI.FONT_SMALL);
        countLabel.setForeground(ModernUI.TEXT_MUTED);
        countLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        left.add(title);
        left.add(countLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD) :");
        lblDate.setFont(ModernUI.FONT_BODY);
        lblDate.setForeground(ModernUI.TEXT_DARK);

        champDate = ModernUI.createTextField(10);
        champDate.setPreferredSize(new Dimension(120, 36));

        JButton btnRechercher = ModernUI.createButton("Rechercher", ModernUI.ACCENT);
        btnRechercher.setPreferredSize(new Dimension(120, 36));
        btnRechercher.addActionListener(e -> controleur.chargerSeancesParDate(champDate.getText()));

        JButton btnTout = ModernUI.createButton("Tout afficher", ModernUI.TEXT_MUTED);
        btnTout.setPreferredSize(new Dimension(140, 36));
        btnTout.addActionListener(e -> {
            champDate.setText("");
            controleur.chargerSeances();
        });

        right.add(lblDate);
        right.add(champDate);
        right.add(btnRechercher);
        right.add(btnTout);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Film", "Salle", "Date", "Heure", "Places", "Statut"};
        modeleTable = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(modeleTable);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernUI.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ModernUI.BG_LIGHT);
        centerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(ModernUI.BG_LIGHT);
        southPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel formCard = ModernUI.createCard();
        formCard.setLayout(new BorderLayout());

        JLabel formTitle = new JLabel("D\u00e9tails de la s\u00e9ance");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(ModernUI.TEXT_DARK);
        formTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 5, 15, 10));
        fields.setOpaque(false);

        champIdFilm = ModernUI.createTextField(8);
        champIdSalle = ModernUI.createTextField(8);
        champDateSeance = ModernUI.createTextField(8);
        champHeure = ModernUI.createTextField(8);
        champPlaces = ModernUI.createTextField(8);

        fields.add(new JLabel("ID Film :"));
        fields.add(new JLabel("ID Salle :"));
        fields.add(new JLabel("Date :"));
        fields.add(new JLabel("Heure :"));
        fields.add(new JLabel("Places :"));
        fields.add(champIdFilm);
        fields.add(champIdSalle);
        fields.add(champDateSeance);
        fields.add(champHeure);
        fields.add(champPlaces);

        formCard.add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        btnAjouter = ModernUI.createButton("Ajouter", ModernUI.SUCCESS);
        btnModifier = ModernUI.createButton("Modifier", ModernUI.WARNING);
        btnSupprimer = ModernUI.createButton("Supprimer", ModernUI.DANGER);

        Dimension btnSize = new Dimension(120, 38);
        btnAjouter.setPreferredSize(btnSize);
        btnModifier.setPreferredSize(btnSize);
        btnSupprimer.setPreferredSize(btnSize);

        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);

        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        southPanel.add(formCard, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int ligne = table.getSelectedRow();
                boolean isSelected = ligne >= 0;
                btnModifier.setEnabled(isSelected);
                btnSupprimer.setEnabled(isSelected);
                if (isSelected) remplirFormulaire(ligne);
            }
        });

        btnAjouter.addActionListener(e -> controleur.ajouterSeance(
                champIdFilm.getText(), champIdSalle.getText(),
                champDateSeance.getText(), champHeure.getText(), champPlaces.getText()));

        btnModifier.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int id = (int) modeleTable.getValueAt(ligne, 0);
                controleur.modifierSeance(id, champIdFilm.getText(), champIdSalle.getText(),
                        champDateSeance.getText(), champHeure.getText(), champPlaces.getText());
            }
        });

        btnSupprimer.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int id = (int) modeleTable.getValueAt(ligne, 0);
                if (ModernUI.showConfirm(this, "Supprimer cette s\u00e9ance ?") == JOptionPane.YES_OPTION) {
                    controleur.supprimerSeance(id, "ID " + id);
                }
            }
        });
    }

    public void afficherSeances(List<Seance> seances) {
        modeleTable.setRowCount(0);
        for (Seance s : seances) {
            String statut = s.estComplete() ? "Complet" : "Libre";
            modeleTable.addRow(new Object[]{
                    s.getIdSeance(), s.getIdFilm(), s.getIdSalle(),
                    s.getDateSeance(), s.getHeure(), s.getPlacesDispo(), statut
            });
        }
        countLabel.setText(seances.size() + " s\u00e9ance(s)");
        if (fenetrePrincipale != null) {
            fenetrePrincipale.setStatut(seances.size() + " s\u00e9ance(s) charg\u00e9e(s)");
        }
    }

    private void remplirFormulaire(int ligne) {
        champIdFilm.setText(modeleTable.getValueAt(ligne, 1).toString());
        champIdSalle.setText(modeleTable.getValueAt(ligne, 2).toString());
        champDateSeance.setText(modeleTable.getValueAt(ligne, 3).toString());
        champHeure.setText(modeleTable.getValueAt(ligne, 4).toString());
        champPlaces.setText(modeleTable.getValueAt(ligne, 5).toString());
    }

    public void afficherSucces(String msg) {
        ModernUI.showSuccess(this, msg);
    }

    public void afficherErreur(String msg) {
        ModernUI.showError(this, msg);
    }

    public void filtrerParDate(String date) {
        champDate.setText(date);
        if (controleur != null) controleur.chargerSeancesParDate(date);
    }
}