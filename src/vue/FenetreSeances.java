package vue;

import controlleur.SeanceControleur;
import modele.Reservation;
import modele.Seance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FenetreSeances extends JPanel {

    private static final long serialVersionUID = 1L;

    private transient FenetrePrincipale fenetrePrincipale;
    private transient SeanceControleur controleur;
    private JTable table;
    private DefaultTableModel modeleTable;
    private JTextField champDate;

    private JTextField champIdFilm, champIdSalle, champDateSeance, champHeure, champPlaces;
    private JButton btnAjouter, btnModifier, btnSupprimer, btnReserver, btnVoirReservations;
    private JLabel countLabel;

    // Champs pour la réservation client
    private JTextField champNomClient, champPrenomClient, champNbPlaces;

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

        // ===== TOOLBAR (haut) =====
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

        // ===== TABLE (centre) =====
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

        // ===== PANNEAU BAS : Formulaire séance + Formulaire réservation =====
        JPanel southPanel = new JPanel(new BorderLayout(0, 10));
        southPanel.setBackground(ModernUI.BG_LIGHT);
        southPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // --- Card 1 : Détails de la séance ---
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

        // --- Card 2 : Réservation client ---
        JPanel reservationCard = ModernUI.createCard();
        reservationCard.setLayout(new BorderLayout());

        JLabel reservTitle = new JLabel("R\u00e9servation client");
        reservTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reservTitle.setForeground(ModernUI.TEXT_DARK);
        reservTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        reservationCard.add(reservTitle, BorderLayout.NORTH);

        JPanel reservFields = new JPanel(new GridLayout(2, 3, 15, 10));
        reservFields.setOpaque(false);

        champNomClient = ModernUI.createTextField(10);
        champPrenomClient = ModernUI.createTextField(10);
        champNbPlaces = ModernUI.createTextField(5);

        reservFields.add(new JLabel("Nom :"));
        reservFields.add(new JLabel("Pr\u00e9nom :"));
        reservFields.add(new JLabel("Nb places :"));
        reservFields.add(champNomClient);
        reservFields.add(champPrenomClient);
        reservFields.add(champNbPlaces);

        reservationCard.add(reservFields, BorderLayout.CENTER);

        JPanel reservBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        reservBtnPanel.setOpaque(false);

        btnReserver = ModernUI.createButton("R\u00e9server", ModernUI.ACCENT);
        btnVoirReservations = ModernUI.createButton("Voir r\u00e9servations", new Color(108, 117, 125));

        btnReserver.setPreferredSize(new Dimension(140, 38));
        btnVoirReservations.setPreferredSize(new Dimension(160, 38));

        btnReserver.setEnabled(false);
        btnVoirReservations.setEnabled(false);

        reservBtnPanel.add(btnReserver);
        reservBtnPanel.add(btnVoirReservations);

        reservationCard.add(reservBtnPanel, BorderLayout.SOUTH);

        // Assemblage du panneau bas
        southPanel.add(formCard, BorderLayout.NORTH);
        southPanel.add(reservationCard, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // ===== ECOUTEURS =====

        // Sélection dans le tableau
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int ligne = table.getSelectedRow();
                boolean isSelected = ligne >= 0;
                btnModifier.setEnabled(isSelected);
                btnSupprimer.setEnabled(isSelected);
                btnReserver.setEnabled(isSelected);
                btnVoirReservations.setEnabled(isSelected);
                if (isSelected) remplirFormulaire(ligne);
            }
        });

        // Boutons séance
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

        // Bouton réservation : envoie nom, prénom, nb places au contrôleur
        btnReserver.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int idSeance = (int) modeleTable.getValueAt(ligne, 0);
                controleur.reserverPlaces(idSeance,
                        champNomClient.getText(), champPrenomClient.getText(),
                        champNbPlaces.getText());
                // Vider les champs après réservation
                champNomClient.setText("");
                champPrenomClient.setText("");
                champNbPlaces.setText("");
            }
        });

        // Bouton voir les réservations d'une séance
        btnVoirReservations.addActionListener(e -> {
            int ligne = table.getSelectedRow();
            if (ligne >= 0) {
                int idSeance = (int) modeleTable.getValueAt(ligne, 0);
                afficherReservationsDialog(idSeance);
            }
        });
    }

    /**
     * Affiche un dialogue listant les réservations pour une séance sélectionnée.
     */
    private void afficherReservationsDialog(int idSeance) {
        List<Reservation> reservations = controleur.getReservationsParSeance(idSeance);

        if (reservations.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucune r\u00e9servation pour cette s\u00e9ance.",
                    "R\u00e9servations - S\u00e9ance #" + idSeance,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Table des réservations
        String[] cols = {"ID", "Nom", "Pr\u00e9nom", "Places", "Date r\u00e9servation", "Par (r\u00f4le)", "ID Compte"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Reservation r : reservations) {
            model.addRow(new Object[]{
                    r.getIdReservation(),
                    r.getNomClient(),
                    r.getPrenomClient(),
                    r.getNbPlaces(),
                    r.getDateReservation() != null ? r.getDateReservation().toString().substring(0, 16) : "",
                    r.getRoleCompte(),
                    r.getIdCompte()
            });
        }

        JTable tableReserv = new JTable(model);
        ModernUI.styleTable(tableReserv);
        JScrollPane scroll = new JScrollPane(tableReserv);
        scroll.setPreferredSize(new Dimension(700, 250));

        // Bouton annuler une réservation
        JButton btnAnnuler = ModernUI.createButton("Annuler la r\u00e9servation s\u00e9lectionn\u00e9e", ModernUI.DANGER);
        btnAnnuler.addActionListener(ev -> {
            int row = tableReserv.getSelectedRow();
            if (row >= 0) {
                int idRes = (int) model.getValueAt(row, 0);
                if (ModernUI.showConfirm(this, "Annuler cette r\u00e9servation ?") == JOptionPane.YES_OPTION) {
                    controleur.annulerReservation(idRes);
                    // Rafraîchir le dialogue
                    model.removeRow(row);
                }
            } else {
                ModernUI.showError(this, "S\u00e9lectionnez une r\u00e9servation.");
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnAnnuler, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel,
                "R\u00e9servations - S\u00e9ance #" + idSeance,
                JOptionPane.PLAIN_MESSAGE);
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