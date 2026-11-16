package controlleur.fx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import modele.Compte;

/**
 * Contrôleur JavaFX de la fenêtre principale (main.fxml).
 * Gère la navigation entre les vues Films, Séances et Gestionnaires.
 */
public class MainFxController implements Initializable {

    // ───── Éléments FXML ──────────────────────────────────────────────────
    @FXML private Label      labelUser;
    @FXML private Label      labelRole;
    @FXML private Button     btnFilms;
    @FXML private Button     btnSeances;
    @FXML private Button     btnSalles;
    @FXML private Button     btnGestionnaires;
    @FXML private BorderPane contentArea;   // Zone centrale où charger les sous-vues

    // ───── État ───────────────────────────────────────────────────────────
    /** Compte de l'utilisateur actuellement connecté */
    private Compte compteConnecte;

    /** Bouton de navigation actuellement actif */
    private Button boutonActif;

    // ───── Initialisation ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisé via setCompteConnecte() après le chargement du FXML
    }

    /**
     * Appelé par ConnexionFxController après l'authentification.
     * Injecte le compte connecté et configure l'interface.
     */
    public void setCompteConnecte(Compte compte) {
        this.compteConnecte = compte;

        // Afficher les infos de l'utilisateur dans l'en-tête
        labelUser.setText(compte.getUsername());
        labelRole.setText(compte.getRole().toUpperCase());

        // Masquer le bouton Gestionnaires pour les non-admins
        boolean estAdmin = "admin".equalsIgnoreCase(compte.getRole());
        btnGestionnaires.setVisible(estAdmin);
        btnGestionnaires.setManaged(estAdmin);

        // Charger la vue Films par défaut
        boutonActif = btnFilms;
        chargerVue("/fxml/films.fxml");
    }

    // ───── Actions FXML (navigation) ──────────────────────────────────────

    /** Navigation vers la gestion des films */
    @FXML
    private void handleFilms() {
        activerBouton(btnFilms);
        chargerVue("/fxml/films.fxml");
    }

    /** Navigation vers la gestion des séances */
    @FXML
    private void handleSeances() {
        activerBouton(btnSeances);
        chargerVue("/fxml/seances.fxml");
    }

    /** Navigation vers la gestion des salles */
    @FXML
    private void handleSalles() {
        activerBouton(btnSalles);
        chargerVue("/fxml/salles.fxml");
    }

    /** Navigation vers la gestion des gestionnaires (admin seulement) */
    @FXML
    private void handleGestionnaires() {
        if ("admin".equalsIgnoreCase(compteConnecte.getRole())) {
            activerBouton(btnGestionnaires);
            chargerVue("/fxml/gestionnaires.fxml");
        }
    }

    /** Déconnexion : ferme la fenêtre principale, ouvre la connexion */
    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/connexion.fxml")
            );
            Parent racine = loader.load();

            Scene scene = new Scene(racine, 440, 560);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );

            Stage stageConnexion = new Stage();
            stageConnexion.setTitle("CinéTrack — Authentification");
            stageConnexion.setScene(scene);
            stageConnexion.setResizable(false);
            stageConnexion.show();

            // Fermer la fenêtre principale
            Stage stageMain = (Stage) contentArea.getScene().getWindow();
            stageMain.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Quitte l'application */
    @FXML
    private void handleQuitter() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    // ───── Méthodes utilitaires ───────────────────────────────────────────

    /**
     * Charge un fichier FXML dans la zone centrale (contentArea).
     * @param cheminFxml chemin classpath du FXML (ex: "/fxml/films.fxml")
     */
    private void chargerVue(String cheminFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(cheminFxml)
            );
            Parent vue = loader.load();

            // Injecter le compte connecté si le contrôleur le supporte
            Object controller = loader.getController();
            if (controller instanceof SeanceFxController seanceCtrl) {
                seanceCtrl.setCompteConnecte(compteConnecte);
            }

            contentArea.setCenter(vue);
        } catch (Exception e) {
            System.err.println("Erreur chargement vue : " + cheminFxml);
            e.printStackTrace();
        }
    }

    /**
     * Met à jour les styles CSS des boutons de navigation
     * pour indiquer le bouton actif.
     */
    private void activerBouton(Button boutonCible) {
        // Désactiver l'ancien bouton actif
        if (boutonActif != null) {
            boutonActif.getStyleClass().remove("sidebar-btn-active");
            if (!boutonActif.getStyleClass().contains("sidebar-btn")) {
                boutonActif.getStyleClass().add("sidebar-btn");
            }
        }

        // Activer le nouveau bouton
        boutonCible.getStyleClass().remove("sidebar-btn");
        if (!boutonCible.getStyleClass().contains("sidebar-btn-active")) {
            boutonCible.getStyleClass().add("sidebar-btn-active");
        }

        boutonActif = boutonCible;
    }
}
