package vue;

import controlleur.FilmControleur;
import modele.Film;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FenetreFilms extends JPanel {

    private JTable table;
    private DefaultTableModel modeleTable;
    private FilmControleur controleur;

    private JTextField champTitre, champDuree, champNote, champSynopsis;
    private JComboBox<String> comboGenre;
    private JButton btnAjouter, btnModifier, btnSupprimer, btnRafraichir;
    private JLabel labelTitre;
    private JLabel countLabel;

    public FenetreFilms() {
        // Constructeur vide pour respecter MVC
    }

    public void initialiser(FilmControleur controleur) {
        this.controleur = controleur;
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernUI.BG_LIGHT);

        construireInterface();
    }

    private void construireInterface() {
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernUI.BORDER),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        labelTitre = new JLabel("Gestion des Films");
        labelTitre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labelTitre.setForeground(ModernUI.TEXT_DARK);
        countLabel = new JLabel();
        countLabel.setFont(ModernUI.FONT_SMALL);
        countLabel.setForeground(ModernUI.TEXT_MUTED);
        countLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        left.add(labelTitre);
        left.add(countLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        String[] genres = { "Tous", "Action", "Com\u00E9die", "Drame",
                "Animation", "Horreur", "Science-Fiction", "Thriller" };
        JComboBox<String> comboFiltreGenre = new JComboBox<>(genres);
        comboFiltreGenre.setFont(ModernUI.FONT_BODY);
        comboFiltreGenre.setBackground(Color.WHITE);

        JButton btnFiltrer = ModernUI.createButton("Filtrer", ModernUI.ACCENT);
        btnFiltrer.setPreferredSize(new Dimension(100, 36));

        btnFiltrer.addActionListener(e -> {
            String genre = (String) comboFiltreGenre.getSelectedItem();
            if ("Tous".equals(genre)) {
                controleur.chargerFilms();
            } else {
                controleur.rechercherParGenre(genre);
            }
        });

        right.add(new JLabel("Genre :"));
        right.add(comboFiltreGenre);
        right.add(btnFiltrer);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        String[] colonnes = { "ID", "Titre", "Genre", "Dur\u00e9e", "Note", "Synopsis" };
        modeleTable = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(modeleTable);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernUI.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                remplirFormulaire();
                btnModifier.setEnabled(table.getSelectedRow() >= 0);
                btnSupprimer.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ModernUI.BG_LIGHT);
        centerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setBackground(ModernUI.BG_LIGHT);
        eastPanel.setBorder(new EmptyBorder(15, 0, 15, 20));

        JPanel formCard = ModernUI.createCard();
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUI.BORDER),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel formTitle = new JLabel("D\u00e9tails du film");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(ModernUI.TEXT_DARK);
        formTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formGrid.add(new JLabel("Titre"), gbc);
        gbc.gridy = 1;
        champTitre = ModernUI.createTextField(15);
        formGrid.add(champTitre, gbc);

        gbc.gridy = 2;
        formGrid.add(new JLabel("Genre"), gbc);
        gbc.gridy = 3;
        comboGenre = ModernUI.createComboBox(new String[] {
                "Action", "Com\u00E9die", "Drame", "Animation",
                "Horreur", "Science-Fiction", "Thriller" });
        formGrid.add(comboGenre, gbc);

        gbc.gridy = 4;
        formGrid.add(new JLabel("Dur\u00e9e (ex: 2h15)"), gbc);
        gbc.gridy = 5;
        champDuree = ModernUI.createTextField(15);
        formGrid.add(champDuree, gbc);

        gbc.gridy = 6;
        formGrid.add(new JLabel("Note (0-10)"), gbc);
        gbc.gridy = 7;
        champNote = ModernUI.createTextField(15);
        formGrid.add(champNote, gbc);

        gbc.gridy = 8;
        formGrid.add(new JLabel("Synopsis"), gbc);
        gbc.gridy = 9;
        champSynopsis = ModernUI.createTextField(15);
        formGrid.add(champSynopsis, gbc);

        formCard.add(formGrid, BorderLayout.CENTER);

        eastPanel.add(formCard, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        btnAjouter = ModernUI.createButton("Ajouter", ModernUI.SUCCESS);
        btnModifier = ModernUI.createButton("Modifier", ModernUI.WARNING);
        btnSupprimer = ModernUI.createButton("Supprimer", ModernUI.DANGER);
        btnRafraichir = ModernUI.createButton("Rafra\u00EEchir", ModernUI.ACCENT);

        Dimension btnSize = new Dimension(110, 38);
        btnAjouter.setPreferredSize(btnSize);
        btnModifier.setPreferredSize(btnSize);
        btnSupprimer.setPreferredSize(btnSize);
        btnRafraichir.setPreferredSize(btnSize);

        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);

        btnAjouter.addActionListener(e -> controleur.ajouterFilm(
                champTitre.getText(), (String) comboGenre.getSelectedItem(),
                champDuree.getText(), champNote.getText(), champSynopsis.getText()));

        btnModifier.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int id = (int) modeleTable.getValueAt(ligne, 0);
                controleur.modifierFilm(id, champTitre.getText(),
                        (String) comboGenre.getSelectedItem(),
                        champDuree.getText(), champNote.getText(), champSynopsis.getText());
            }
        });

        btnSupprimer.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int id = (int) modeleTable.getValueAt(ligne, 0);
                String titre = (String) modeleTable.getValueAt(ligne, 1);
                if (ModernUI.showConfirm(this, "Supprimer \"" + titre + "\" ?") == JOptionPane.YES_OPTION) {
                    controleur.supprimerFilm(id, titre);
                }
            }
        });

        btnRafraichir.addActionListener(e -> controleur.chargerFilms());

        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        btnPanel.add(Box.createHorizontalStrut(5));
        btnPanel.add(btnRafraichir);

        eastPanel.add(btnPanel, BorderLayout.SOUTH);

        add(eastPanel, BorderLayout.EAST);
    }

    public void afficherFilms(List<Film> films) {
        modeleTable.setRowCount(0);
        for (Film f : films) {
            String noteAffichage = f.getNote() > 0 ? String.valueOf(f.getNote()) : "-";
            modeleTable.addRow(new Object[] {
                    f.getIdFilm(), f.getTitre(), f.getGenre(),
                    f.getDureeFormatee(), noteAffichage, f.getSynopsis()
            });
        }
        mettreAJourTitre(films.size());
    }

    public void afficherSucces(String message) {
        ModernUI.showSuccess(this, message);
    }

    public void afficherErreur(String message) {
        ModernUI.showError(this, message);
    }

    public void mettreAJourTitre(int nbFilms) {
        countLabel.setText(nbFilms + " film(s)");
    }

    public void rechercherParGenre(String genre) {
        if (controleur != null)
            controleur.rechercherParGenre(genre);
    }

    private void remplirFormulaire() {
        int ligne = table.getSelectedRow();
        if (ligne >= 0) {
            champTitre.setText((String) modeleTable.getValueAt(ligne, 1));
            String genre = (String) modeleTable.getValueAt(ligne, 2);
            comboGenre.setSelectedItem(genre);
            champDuree.setText(String.valueOf(modeleTable.getValueAt(ligne, 3)));
            champNote.setText(String.valueOf(modeleTable.getValueAt(ligne, 4)));
            champSynopsis.setText((String) modeleTable.getValueAt(ligne, 5));
        }
    }
}