package controlleur.fx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import modele.Compte;
import service.CompteService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur JavaFX de la vue Gestionnaires (gestionnaires.fxml).
 * Réservé aux administrateurs : ajout et suppression de comptes gestionnaires.
 */
public class GestionnaireFxController implements Initializable {

    // ───── Éléments FXML ──────────────────────────────────────────────────
    @FXML private TableView<Compte>             tableGestionnaires;
    @FXML private TableColumn<Compte, Integer>  colIdCompte;
    @FXML private TableColumn<Compte, String>   colUsername;
    @FXML private TableColumn<Compte, String>   colRoleCompte;

    @FXML private TextField     champUsername;
    @FXML private PasswordField champPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private Label         labelMessage;

    // ───── Service ────────────────────────────────────────────────────────
    private final CompteService compteService = new CompteService();

    // ───── Initialisation ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colIdCompte.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUsername.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getUsername()));
        colRoleCompte.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getRole()));

        comboRole.setItems(FXCollections.observableArrayList("Caissier", "Manager"));
        comboRole.getSelectionModel().selectFirst();

        chargerGestionnaires();
    }

    // ───── Chargement ─────────────────────────────────────────────────────

    private void chargerGestionnaires() {
        try {
            List<Compte> comptes = compteService.getTousLesGestionnaires();
            tableGestionnaires.setItems(FXCollections.observableArrayList(comptes));
            afficherMessage("", false);
        } catch (Exception e) {
            afficherMessage("Erreur de chargement : " + e.getMessage(), true);
        }
    }

    // ───── Actions FXML ───────────────────────────────────────────────────

    @FXML
    private void handleRafraichir() {
        chargerGestionnaires();
    }

    @FXML
    private void handleAjouter() {
        String username = champUsername.getText().trim();
        String password = champPassword.getText();
        String role = comboRole.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            afficherMessage("Identifiant et mot de passe obligatoires.", true);
            return;
        }

        try {
            compteService.ajouterGestionnaire(username, password, role);
            afficherMessage("✔ Compte '" + username + "' (" + role + ") ajouté.", false);
            champUsername.clear();
            champPassword.clear();
            comboRole.getSelectionModel().selectFirst();
            chargerGestionnaires();
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSupprimer() {
        Compte selectionne = tableGestionnaires.getSelectionModel().getSelectedItem();
        if (selectionne == null) {
            afficherMessage("Sélectionnez un gestionnaire dans le tableau.", true);
            return;
        }

        // Empêcher la suppression de l'admin
        if ("admin".equalsIgnoreCase(selectionne.getRole())) {
            afficherMessage("Impossible de supprimer le compte admin.", true);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer le gestionnaire '" + selectionne.getUsername() + "' ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                try {
                    compteService.supprimerGestionnaire(selectionne.getId());
                    afficherMessage("✔ Gestionnaire supprimé.", false);
                    chargerGestionnaires();
                } catch (Exception e) {
                    afficherMessage("Erreur : " + e.getMessage(), true);
                }
            }
        });
    }

    // ───── Utilitaires ────────────────────────────────────────────────────

    private void afficherMessage(String message, boolean estErreur) {
        labelMessage.setText(message);
        labelMessage.getStyleClass().removeAll("label-erreur", "label-succes");
        if (!message.isEmpty()) {
            labelMessage.getStyleClass().add(estErreur ? "label-erreur" : "label-succes");
        }
    }
}
