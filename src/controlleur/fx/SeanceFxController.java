package controlleur.fx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import modele.Seance;
import service.SeanceService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private TextField   champPlaces;

    // Champs de réservation
    @FXML private TextField   champNomClient;
    @FXML private TextField   champPrenomClient;
    @FXML private TextField   champNbPlaces;

    @FXML private Label       labelTitreFormulaire;
    @FXML private Label       labelMessage;

    // ───── Service & état ─────────────────────────────────────────────────
    private final SeanceService seanceService = new SeanceService();
    private int idSeanceSelectionne = -1;
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
        champIdFilm.setText(String.valueOf(s.getIdFilm()));
        champIdSalle.setText(String.valueOf(s.getIdSalle()));
        champDate.setText(s.getDateSeance());
        champHeure.setText(s.getHeure());
        champPlaces.setText(String.valueOf(s.getPlacesDispo()));
        labelTitreFormulaire.setText("Modifier la séance");
        afficherMessage("Séance #" + s.getIdSeance() + " sélectionnée.", false);
    }

    @FXML
    private void handleAjouter() {
        try {
            // SeanceService.ajouterSeance prend des String directement
            seanceService.ajouterSeance(
                champIdFilm.getText().trim(),
                champIdSalle.getText().trim(),
                champDate.getText().trim(),
                champHeure.getText().trim(),
                champPlaces.getText().trim()
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
            // SeanceService.modifierSeance prend id + String
            seanceService.modifierSeance(
                idSeanceSelectionne,
                champIdFilm.getText().trim(),
                champIdSalle.getText().trim(),
                champDate.getText().trim(),
                champHeure.getText().trim(),
                champPlaces.getText().trim()
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

        if (compteConnecte == null) {
            compteConnecte = new modele.Compte(1, "gestionnaire", "", "gestionnaire");
        }

        try {
            boolean succes = seanceService.reserverPlaces(idSeanceSelectionne, nom, prenom, nbPlaces, compteConnecte);
            if (succes) {
                afficherMessage("✔ Réservation effectuée pour " + prenom + " " + nom + " !", false);
                champNomClient.clear();
                champPrenomClient.clear();
                champNbPlaces.clear();
                chargerSeances(); // Met à jour le nombre de places restantes
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

    private void viderFormulaire() {
        idSeanceSelectionne = -1;
        champIdFilm.clear();
        champIdSalle.clear();
        champDate.clear();
        champHeure.clear();
        champPlaces.clear();
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
