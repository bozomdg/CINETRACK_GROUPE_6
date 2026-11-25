package controlleur.fx;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import modele.Seance;
import service.SeanceService;

/**
 * Contrôleur JavaFX de la vue Séances (seances.fxml).
 * Gère l'affichage et la gestion des séances.
 */
public class SeanceFxController implements Initializable {

    // ───── Éléments FXML ──────────────────────────────────────────────────
    @FXML private TableView<Seance>                tableSeances;
    @FXML private TableColumn<Seance, Integer>     colIdSeance;
    @FXML private TableColumn<Seance, Integer>     colIdFilm;
    @FXML private TableColumn<Seance, Integer>     colIdSalle;
    @FXML private TableColumn<Seance, String>      colDate;
    @FXML private TableColumn<Seance, String>      colHeure;
    @FXML private TableColumn<Seance, Integer>     colPlaces;
    @FXML private TableColumn<Seance, String>      colStatut;

    @FXML private TextField   champFiltreDate;
    @FXML private TextField   champIdFilm;
    @FXML private TextField   champIdSalle;
    @FXML private TextField   champDate;
    @FXML private TextField   champHeure;

    // Champs de réservation
    @FXML private TextField        champNomClient;
    @FXML private TextField        champPrenomClient;
    @FXML private TextField        champNbPlaces;
    @FXML private ComboBox<String> comboTarif;

    @FXML private Label       labelTitreFormulaire;
    @FXML private Label       labelMessage;

    // ───── Service & état ─────────────────────────────────────────────────
    private final SeanceService seanceService = new SeanceService();
    private int idSeanceSelectionne = -1;
    private int placesDispoSelectionnee = 0;
    private modele.Compte compteConnecte;

    public void setCompteConnecte(modele.Compte compte) {
        this.compteConnecte = compte;
    }

    // ───── Initialisation ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurer les colonnes avec PropertyValueFactory-style via lambda
        // (Seance n'a pas de SimpleProperty donc on utilise des lambdas)
        colIdSeance.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getIdSeance()).asObject());
        colIdFilm.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getIdFilm()).asObject());
        colIdSalle.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getIdSalle()).asObject());
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateSeance()));
        colHeure.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getHeure()));
        colPlaces.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPlacesDispo()).asObject());
        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().estComplete() ? "COMPLET" : "Disponible"
                ));

        // Colonne Statut colorée (rouge si complet, vert si dispo)
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("COMPLET".equals(item)
                            ? "-fx-text-fill: #dc143c; -fx-font-weight: bold;"
                            : "-fx-text-fill: #28a745; -fx-font-weight: bold;");
                }
            }
        });

        // Remplir le ComboBox des tarifs
        if (comboTarif != null) {
            comboTarif.setItems(FXCollections.observableArrayList(
                    "NORMAL - 350 Rs",
                    "ETUDIANT - 250 Rs",
                    "ENFANT - 200 Rs",
                    "SENIOR - 250 Rs"
            ));
            comboTarif.getSelectionModel().selectFirst();
        }

        chargerSeances();
    }

    // ───── Chargement ─────────────────────────────────────────────────────
    private void chargerSeances() {
        try {
            List<Seance> seances = seanceService.getAllSeances();
            tableSeances.setItems(FXCollections.observableArrayList(seances));
            afficherMessage("", false);
        } catch (Exception e) {
            afficherMessage("Erreur de chargement : " + e.getMessage(), true);
        }
    }

    // ───── Actions FXML ───────────────────────────────────────────────────
    @FXML
    private void handleRafraichir() {
        viderFormulaire();
        chargerSeances();
    }

    @FXML
    private void handleFiltrerDate() {
        String date = champFiltreDate.getText().trim();
        if (date.isEmpty()) {
            chargerSeances();
            return;
        }
        try {
            List<Seance> seances = seanceService.getSeancesParDate(date);
            tableSeances.setItems(FXCollections.observableArrayList(seances));
        } catch (Exception e) {
            afficherMessage("Erreur filtre : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSelectionSeance(MouseEvent event) {
        Seance s = tableSeances.getSelectionModel().getSelectedItem();
        if (s == null) return;

        idSeanceSelectionne = s.getIdSeance();
        placesDispoSelectionnee = s.getPlacesDispo();

        champIdFilm.setText(String.valueOf(s.getIdFilm()));
        champIdSalle.setText(String.valueOf(s.getIdSalle()));
        champDate.setText(s.getDateSeance());
        champHeure.setText(s.getHeure());

        labelTitreFormulaire.setText("Modifier la séance");
        afficherMessage("Séance #" + s.getIdSeance() + " sélectionnée.", false);
    }

    @FXML
    private void handleAjouter() {
        try {
            // Les places sont calculées automatiquement selon la capacité de la salle
            seanceService.ajouterSeance(
                    champIdFilm.getText().trim(),
                    champIdSalle.getText().trim(),
                    champDate.getText().trim(),
                    champHeure.getText().trim()
            );
            afficherMessage("✔ Séance ajoutée avec succès.", false);
            viderFormulaire();
            chargerSeances();
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleModifier() {
        if (idSeanceSelectionne < 0) {
            afficherMessage("Sélectionnez une séance dans le tableau.", true);
            return;
        }
        try {
            seanceService.modifierSeance(
                    idSeanceSelectionne,
                    champIdFilm.getText().trim(),
                    champIdSalle.getText().trim(),
                    champDate.getText().trim(),
                    champHeure.getText().trim(),
                    placesDispoSelectionnee
            );
            afficherMessage("✔ Séance modifiée avec succès.", false);
            viderFormulaire();
            chargerSeances();
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (idSeanceSelectionne < 0) {
            afficherMessage("Sélectionnez une séance dans le tableau.", true);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer la séance #" + idSeanceSelectionne + " ?");
        confirmation.setContentText("Cette action est irréversible.");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                try {
                    seanceService.supprimerSeance(idSeanceSelectionne);
                    afficherMessage("✔ Séance supprimée.", false);
                    viderFormulaire();
                    chargerSeances();
                } catch (Exception e) {
                    afficherMessage("Erreur : " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    private void handleVider() {
        viderFormulaire();
    }

    // ───── Réservations ───────────────────────────────────────────────────
    @FXML
    private void handleReserver() {
        if (idSeanceSelectionne < 0) {
            afficherMessage("Sélectionnez d'abord une séance dans le tableau.", true);
            return;
        }
        String nom = champNomClient.getText().trim();
        String prenom = champPrenomClient.getText().trim();
        String nbPlaces = champNbPlaces.getText().trim();
        String categorieTarif = getCategorieTarif();
        int prixUnitaire = getPrixTarif();

        if (compteConnecte == null) {
            compteConnecte = new modele.Compte(1, "gestionnaire", "", "gestionnaire");
        }

        try {
            boolean succes = seanceService.reserverPlaces(
                    idSeanceSelectionne, nom, prenom, nbPlaces, compteConnecte
            );
            // Si ton SeanceService accepte aussi le tarif en paramètre, remplace l'appel ci-dessus par :
            // boolean succes = seanceService.reserverPlaces(idSeanceSelectionne, nom, prenom, nbPlaces, categorieTarif, compteConnecte);

            if (succes) {
                int nb = Integer.parseInt(nbPlaces);
                int total = nb * prixUnitaire;
                afficherMessage("✔ Réservation effectuée pour " + prenom + " " + nom
                        + " (" + categorieTarif + " - Total : " + total + " Rs) !", false);
                champNomClient.clear();
                champPrenomClient.clear();
                champNbPlaces.clear();
                if (comboTarif != null) {
                    comboTarif.getSelectionModel().selectFirst();
                }
                chargerSeances(); // Met à jour le nombre de places restantes
            } else {
                afficherMessage("Échec de la réservation.", true);
            }
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    /** Ouvre le plan de la salle pour choisir les places en cliquant dessus */
    @FXML
    private void handleChoisirPlaces() {
        Seance s = tableSeances.getSelectionModel().getSelectedItem();
        if (s == null) {
            afficherMessage("Sélectionnez d'abord une séance dans le tableau.", true);
            return;
        }
        String nom = champNomClient.getText().trim();
        String prenom = champPrenomClient.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty()) {
            afficherMessage("Renseignez le nom et le prénom du client avant de choisir les places.", true);
            return;
        }
        String categorieTarif = getCategorieTarif();
        int prixUnitaire = getPrixTarif();

        if (compteConnecte == null) {
            compteConnecte = new modele.Compte(1, "gestionnaire", "", "gestionnaire");
        }
        try {
            int capacite = seanceService.getCapaciteSalle(s.getIdSalle());
            Set<String> occupees = seanceService.getPlacesOccupees(s.getIdSeance());
            String titre = "Séance #" + s.getIdSeance() + " — " + s.getDateSeance() + " à " + s.getHeure();
            List<String> choix = PlanSalleDialog.afficher(
                    tableSeances.getScene().getWindow(), titre, capacite, occupees);
            if (choix.isEmpty()) {
                return; // annulé
            }
            boolean succes = seanceService.reserverPlacesChoisies(
                    s.getIdSeance(), nom, prenom, choix, compteConnecte);
            if (succes) {
                int total = choix.size() * prixUnitaire;
                afficherMessage("✔ Places " + String.join(", ", choix)
                        + " réservées pour " + prenom + " " + nom
                        + " (" + categorieTarif + " - Total : " + total + " Rs) !", false);
                champNomClient.clear();
                champPrenomClient.clear();
                champNbPlaces.clear();
                if (comboTarif != null) {
                    comboTarif.getSelectionModel().selectFirst();
                }
                chargerSeances();
            } else {
                afficherMessage("Échec de la réservation.", true);
            }
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleVoirReservations() {
        if (idSeanceSelectionne < 0) {
            afficherMessage("Sélectionnez une séance pour voir ses réservations.", true);
            return;
        }
        try {
            List<modele.Reservation> reservations = seanceService.getReservationsParSeance(idSeanceSelectionne);
            if (reservations.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Réservations - Séance #" + idSeanceSelectionne);
                info.setHeaderText("Aucune réservation");
                info.setContentText("Il n'y a actuellement aucune réservation pour cette séance.");
                info.showAndWait();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-20s | %-8s\n", "ID Res", "Client", "Places"));
            sb.append("------------------------------------------\n");
            for (modele.Reservation r : reservations) {
                sb.append(String.format("#%-5d | %-20s | %-8d\n",
                        r.getIdReservation(),
                        r.getPrenomClient() + " " + r.getNomClient(),
                        r.getNbPlaces()));
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservations - Séance #" + idSeanceSelectionne);
            alert.setHeaderText("Liste des réservations (" + reservations.size() + ") :");
            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: monospace;");
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    // ───── Utilitaires ────────────────────────────────────────────────────
    /** Retourne la catégorie du tarif sélectionné (ex: "NORMAL") */
    private String getCategorieTarif() {
        if (comboTarif == null || comboTarif.getValue() == null) {
            return "NORMAL";
        }
        return comboTarif.getValue().split(" - ")[0].trim();
    }

    /** Retourne le prix unitaire en Rs selon le tarif sélectionné */
    private int getPrixTarif() {
        return switch (getCategorieTarif()) {
            case "ETUDIANT", "SENIOR" -> 250;
            case "ENFANT" -> 200;
            default -> 350; // NORMAL
        };
    }

    private void viderFormulaire() {
        idSeanceSelectionne = -1;
        placesDispoSelectionnee = 0;
        champIdFilm.clear();
        champIdSalle.clear();
        champDate.clear();
        champHeure.clear();
        labelTitreFormulaire.setText("Ajouter une séance");
        tableSeances.getSelectionModel().clearSelection();
        afficherMessage("", false);
    }

    private void afficherMessage(String message, boolean estErreur) {
        labelMessage.setText(message);
        labelMessage.getStyleClass().removeAll("label-erreur", "label-succes");
        if (!message.isEmpty()) {
            labelMessage.getStyleClass().add(estErreur ? "label-erreur" : "label-succes");
        }
    }
}