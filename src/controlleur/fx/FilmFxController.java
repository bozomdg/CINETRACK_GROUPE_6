package controlleur.fx;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import modele.Film;
import service.FilmService;

public class FilmFxController implements Initializable {

    // elements du fichier FXML
    @FXML private TableView<Film> tableFilms;
    @FXML private TableColumn<Film, Integer> colId;
    @FXML private TableColumn<Film, String> colTitre;
    @FXML private TableColumn<Film, String> colGenre;
    @FXML private TableColumn<Film, String> colDuree;
    @FXML private TableColumn<Film, String> colSynopsis;

    @FXML private ComboBox<String> comboFiltreGenre;
    @FXML private ComboBox<String> comboGenre;

    @FXML private TextField champTitre;
    @FXML private TextField champDuree;
    @FXML private TextField champSynopsis;

    @FXML private Label labelTitreFormulaire;
    @FXML private Label labelMessage;

    private FilmService serviceFilm = new FilmService();

    // id du film choisi pour la modification (-1 = aucun)
    private int idFilmSelectionne = -1;

    private ObservableList<String> listeGenres = FXCollections.observableArrayList(
            "Action", "Comédie", "Drame", "Horreur",
            "Science-Fiction", "Thriller", "Animation",
            "Documentaire", "Romance", "Aventure");

    // methode imposee par l'interface Initializable (le nom ne peut pas changer)
    @Override
    public void initialize(URL emplacement, ResourceBundle ressources) {
        // colonnes du tableau
        colId.setCellValueFactory(new PropertyValueFactory<>("idFilm"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuree.setCellValueFactory(donnees -> new SimpleStringProperty(donnees.getValue().getDureeFormatee()));
        colSynopsis.setCellValueFactory(new PropertyValueFactory<>("synopsis"));

        // liste des genres du formulaire
        comboGenre.setItems(listeGenres);

        // liste des genres du filtre (avec "Tous les genres" en premier)
        ObservableList<String> genresFiltre = FXCollections.observableArrayList("Tous les genres");
        genresFiltre.addAll(listeGenres);
        comboFiltreGenre.setItems(genresFiltre);
        comboFiltreGenre.getSelectionModel().selectFirst();

        chargerFilms();
    }

    // charge les films dans le tableau (en tenant compte du filtre choisi)
    private void chargerFilms() {
        try {
            String genreChoisi = comboFiltreGenre.getValue();
            List<Film> films;

            if (genreChoisi == null || genreChoisi.equals("Tous les genres")) {
                films = serviceFilm.recupererTousLesFilms();
            } else {
                films = serviceFilm.rechercherParGenre(genreChoisi);
            }

            tableFilms.setItems(FXCollections.observableArrayList(films));
        } catch (Exception e) {
            afficherMessage("Erreur de chargement : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionRafraichir() {
        viderFormulaire();
        chargerFilms();
    }

    @FXML
    private void actionFiltrer() {
        afficherMessage("", false);
        chargerFilms();
    }

    // quand on clique sur une ligne du tableau, on remplit le formulaire
    @FXML
    private void actionSelectionFilm(MouseEvent evenement) {
        Film film = tableFilms.getSelectionModel().getSelectedItem();
        if (film == null) {
            return;
        }

        idFilmSelectionne = film.getIdFilm();
        champTitre.setText(film.getTitre());
        comboGenre.setValue(film.getGenre());
        champDuree.setText(film.getDureeFormatee());
        champSynopsis.setText(film.getSynopsis());

        labelTitreFormulaire.setText("Modifier le film");
        afficherMessage("Film sélectionné : " + film.getTitre(), false);
    }

    @FXML
    private void actionAjouter() {
        String titre = champTitre.getText().trim();
        String genre = comboGenre.getValue();
        String duree = champDuree.getText().trim();
        String synopsis = champSynopsis.getText().trim();

        if (titre.isEmpty() || genre == null || duree.isEmpty()) {
            afficherMessage("Titre, genre et durée sont obligatoires.", true);
            return;
        }

        try {
            serviceFilm.ajouterFilm(titre, genre, duree, synopsis);

            // le message vient en dernier, sinon il est effacé
            viderFormulaire();
            chargerFilms();
            afficherMessage("✔ Film ajouté avec succès.", false);
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionModifier() {
        if (idFilmSelectionne < 0) {
            afficherMessage("Sélectionnez un film dans le tableau.", true);
            return;
        }

        String titre = champTitre.getText().trim();
        String genre = comboGenre.getValue();
        String duree = champDuree.getText().trim();
        String synopsis = champSynopsis.getText().trim();

        if (titre.isEmpty() || genre == null || duree.isEmpty()) {
            afficherMessage("Titre, genre et durée sont obligatoires.", true);
            return;
        }

        try {
            serviceFilm.modifierFilm(idFilmSelectionne, titre, genre, duree, synopsis);

            viderFormulaire();
            chargerFilms();
            afficherMessage("✔ Film modifié avec succès.", false);
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void actionSupprimer() {
        if (idFilmSelectionne < 0) {
            afficherMessage("Sélectionnez un film dans le tableau.", true);
            return;
        }

        // confirmation avant de supprimer
        Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
        alerte.setTitle("Confirmer la suppression");
        alerte.setHeaderText("Supprimer ce film ?");
        alerte.setContentText("Cette action est irréversible.");

        Optional<ButtonType> reponse = alerte.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                serviceFilm.supprimerFilm(idFilmSelectionne);

                viderFormulaire();
                chargerFilms();
                afficherMessage("✔ Film supprimé.", false);
            } catch (Exception e) {
                afficherMessage("Erreur : " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void actionVider() {
        viderFormulaire();
    }

    // remet le formulaire à zéro
    private void viderFormulaire() {
        idFilmSelectionne = -1;
        champTitre.clear();
        comboGenre.getSelectionModel().clearSelection();
        champDuree.clear();
        champSynopsis.clear();
        labelTitreFormulaire.setText("Ajouter un film");
        tableFilms.getSelectionModel().clearSelection();
        afficherMessage("", false);
    }

    // affiche un message (rouge si erreur, vert sinon)
    private void afficherMessage(String message, boolean estErreur) {
        labelMessage.setText(message);
        labelMessage.getStyleClass().removeAll("label-erreur", "label-succes");

        if (!message.isEmpty()) {
            if (estErreur) {
                labelMessage.getStyleClass().add("label-erreur");
            } else {
                labelMessage.getStyleClass().add("label-succes");
            }
        }
    }

    // Alias pour compatibilité
    @FXML private void handleRafraichir() { actionRafraichir(); }
    @FXML private void handleFiltrer() { actionFiltrer(); }
    @FXML private void handleSelectionFilm(MouseEvent e) { actionSelectionFilm(e); }
    @FXML private void handleAjouter() { actionAjouter(); }
    @FXML private void handleModifier() { actionModifier(); }
    @FXML private void handleSupprimer() { actionSupprimer(); }
    @FXML private void handleVider() { actionVider(); }
}