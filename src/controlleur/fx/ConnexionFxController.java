package controlleur.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import modele.Compte;
import service.CompteService;
import exception.ValidationException;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur JavaFX de la vue de connexion (connexion.fxml).
 * Vérifie les identifiants via CompteService et ouvre la fenêtre principale.
 */
public class ConnexionFxController implements Initializable {

    // ───── Éléments FXML liés ─────────────────────────────────────────────
    @FXML private TextField     fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private Button        buttonSeConnecter;
    @FXML private Label         labelErreur;

    // ───── Services ───────────────────────────────────────────────────────
    /** Service de gestion des comptes (injection via constructeur par défaut) */
    private final CompteService compteService = new CompteService();

    // ───── Initialisation ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vide le message d'erreur au démarrage
        labelErreur.setText("");

        // Appuyer sur Entrée dans le champ username → focus sur le mot de passe
        fieldUsername.setOnAction(e -> fieldPassword.requestFocus());
    }

    // ───── Actions FXML ───────────────────────────────────────────────────

    /**
     * Appelé quand l'utilisateur clique sur "SE CONNECTER"
     * ou appuie sur Entrée dans le champ mot de passe.
     */
    @FXML
    private void handleSeConnecter() {
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText();

        // Tentative d'authentification et validation via le service
        try {
            Compte compte = compteService.authentifier(username, password);
            // Connexion réussie → ouvrir la fenêtre principale
            ouvrirFenetrePrincipale(compte);
        } catch (ValidationException e) {
            // Identifiants incorrects ou champs vides
            labelErreur.setText(e.getMessage());
            fieldPassword.clear();
            fieldPassword.requestFocus();
        }
    }

    // ───── Navigation ─────────────────────────────────────────────────────

    /**
     * Charge main.fxml, passe le compte connecté au contrôleur principal,
     * ferme la fenêtre de connexion.
     */
    private void ouvrirFenetrePrincipale(Compte compte) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
            );
            Parent racine = loader.load();

            // Passer le compte connecté au contrôleur principal
            MainFxController mainCtrl = loader.getController();
            mainCtrl.setCompteConnecte(compte);

            // Créer et afficher la nouvelle scène
            Scene scene = new Scene(racine, 1100, 680);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );

            Stage stageMain = new Stage();
            stageMain.setTitle("CinéTrack — Backoffice");
            stageMain.setScene(scene);
            stageMain.show();

            // Fermer la fenêtre de connexion
            Stage stageConnexion = (Stage) buttonSeConnecter.getScene().getWindow();
            stageConnexion.close();

        } catch (Exception e) {
            labelErreur.setText("Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
