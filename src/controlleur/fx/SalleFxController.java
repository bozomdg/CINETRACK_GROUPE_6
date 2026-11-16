package controlleur.fx;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import modele.Salle;
import service.SalleService;

/**
 * Contrôleur JavaFX pour la gestion des salles (salles.fxml).
 */
public class SalleFxController implements Initializable {

    // Éléments FXML
    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, Integer> colIdSalle;
    @FXML private TableColumn<Salle, String>  colNomSalle;
    @FXML private TableColumn<Salle, Integer> colCapacite;

    @FXML private TextField champNom;
    @FXML private TextField champCapacite;

    @FXML private Label labelTitreFormulaire;
    @FXML private Label labelMessage;

    // Service métier pour les salles
    private SalleService serviceSalle = new SalleService();

    // ID de la salle sélectionnée (-1 = aucune)
    private int idSalleSelectionnee = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuration des colonnes
        colIdSalle.setCellValueFactory(donnees ->
            new SimpleIntegerProperty(donnees.getValue().getIdSalle()).asObject());
        colNomSalle.setCellValueFactory(donnees ->
            new SimpleStringProperty(donnees.getValue().getNom()));
        colCapacite.setCellValueFactory(donnees ->
            new SimpleIntegerProperty(donnees.getValue().getCapacite()).asObject());

        chargerSalles();
    }

    // Charge les salles dans le tableau
    private void chargerSalles() {
        try {
            List<Salle> salles = serviceSalle.getAllSalles();
            tableSalles.setItems(FXCollections.observableArrayList(salles));
        } catch (Exception e) {
            afficherMessage("Erreur de chargement : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionRafraichir() {
        viderFormulaire();
        chargerSalles();
    }

    // Clic sur une ligne du tableau -> remplit le formulaire
    @FXML
    private void actionSelectionSalle(MouseEvent evenement) {
        Salle salle = tableSalles.getSelectionModel().getSelectedItem();
        if (salle == null) return;

        idSalleSelectionnee = salle.getIdSalle();
        champNom.setText(salle.getNom());
        champCapacite.setText(String.valueOf(salle.getCapacite()));

        labelTitreFormulaire.setText("Modifier la salle");
        afficherMessage("Salle #" + salle.getIdSalle() + " sélectionnée.", false);
    }

    @FXML
    private void actionAjouter() {
        String nom = champNom.getText().trim();
        String capacite = champCapacite.getText().trim();

        if (nom.isEmpty() || capacite.isEmpty()) {
            afficherMessage("Tous les champs sont obligatoires.", true);
            return;
        }

        try {
            serviceSalle.ajouterSalle(nom, capacite);
            viderFormulaire();
            chargerSalles();
            afficherMessage("✔ Salle ajoutée avec succès.", false);
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionModifier() {
        if (idSalleSelectionnee < 0) {
            afficherMessage("Sélectionnez une salle dans le tableau.", true);
            return;
        }

        String nom = champNom.getText().trim();
        String capacite = champCapacite.getText().trim();

        if (nom.isEmpty() || capacite.isEmpty()) {
            afficherMessage("Tous les champs sont obligatoires.", true);
            return;
        }

        try {
            serviceSalle.modifierSalle(idSalleSelectionnee, nom, capacite);
            viderFormulaire();
            chargerSalles();
            afficherMessage("✔ Salle modifiée avec succès.", false);
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionSupprimer() {
        if (idSalleSelectionnee < 0) {
            afficherMessage("Sélectionnez une salle dans le tableau.", true);
            return;
        }

        Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
        alerte.setTitle("Confirmer la suppression");
        alerte.setHeaderText("Supprimer la salle #" + idSalleSelectionnee + " ?");
        alerte.setContentText("Cette action est irréversible.");

        Optional<ButtonType> reponse = alerte.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                serviceSalle.supprimerSalle(idSalleSelectionnee);
                viderFormulaire();
                chargerSalles();
                afficherMessage("✔ Salle supprimée.", false);
            } catch (Exception e) {
                afficherMessage("Erreur : " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void actionVider() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        idSalleSelectionnee = -1;
        champNom.clear();
        champCapacite.clear();
        labelTitreFormulaire.setText("Ajouter une salle");
        tableSalles.getSelectionModel().clearSelection();
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
