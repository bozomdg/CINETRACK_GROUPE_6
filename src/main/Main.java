package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main {

    // Point d'entrée standard de l'application
    public static void main(String[] args) {
        Application.launch(FenetrePrincipaleApp.class, args);
    }

    // Classe JavaFX interne qui charge l'interface
    public static class FenetrePrincipaleApp extends Application {

        @Override
        public void start(Stage stagePrincipal) throws Exception {
            // Chargement du fichier FXML de connexion
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/connexion.fxml")
            );
            Parent racine = loader.load();

            // Application du fichier de style CSS
            Scene scene = new Scene(racine, 440, 560);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );

            // Configuration et affichage de la fenêtre
            stagePrincipal.setTitle("CinéTrack: Authentification");
            stagePrincipal.setScene(scene);
            stagePrincipal.setResizable(false);
            stagePrincipal.show();
        }
    }
}
