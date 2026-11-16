package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX de CinéTrack.
 * Lance la fenêtre de connexion (connexion.fxml).
 * Architecture MVC : MainApp → connexion.fxml → ConnexionFxController
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stagePrincipal) throws Exception {

        // Chargement du FXML de connexion (depuis le classpath)
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/connexion.fxml")
        );
        Parent racine = loader.load();

        // Création de la scène avec le CSS global
        Scene scene = new Scene(racine, 440, 560);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );

        // Configuration du Stage (fenêtre principale)
        stagePrincipal.setTitle("CinéTrack — Authentification");
        stagePrincipal.setScene(scene);
        stagePrincipal.setResizable(false);
        stagePrincipal.show();
    }

    /** Méthode main : nécessaire pour certains environnements sans module-info */
    public static void main(String[] args) {
        launch(args);
    }
}
