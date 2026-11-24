
package controlleur.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Fenêtre affichant le plan de la salle.
 * L'utilisateur clique sur les fauteuils libres pour les sélectionner.
 * Rouge = déjà réservé, gris = libre, vert = sélectionné.
 */
public class PlanSalleDialog {

    private static final int PLACES_PAR_RANGEE = 10;
    private static final int COLONNE_ALLEE = PLACES_PAR_RANGEE / 2; // allée au milieu

    private static final String STYLE_LIBRE =
            "-fx-background-color: #d9d9d9; -fx-text-fill: #333; -fx-font-size: 10px; "
                    + "-fx-background-radius: 6; -fx-cursor: hand;";
    private static final String STYLE_SELECTIONNE =
            "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
    private static final String STYLE_OCCUPE =
            "-fx-background-color: #dc143c; -fx-text-fill: white; -fx-font-size: 10px; "
                    + "-fx-background-radius: 6; -fx-opacity: 0.85;";

    /**
     * Affiche le plan et retourne la liste des places choisies
     * (liste vide si l'utilisateur annule).
     */
    public static List<String> afficher(Window parent, String titre, int capacite, Set<String> occupees) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Plan de la salle");
        dialog.setHeaderText(titre);
        if (parent != null) {
            dialog.initOwner(parent);
        }

        ButtonType btnValider = new ButtonType("Valider les places", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        Set<String> choisies = new TreeSet<>((a, b) -> comparerPlaces(a, b));
        Label labelSelection = new Label("Aucune place sélectionnée");
        labelSelection.setStyle("-fx-font-weight: bold;");
        labelSelection.setWrapText(true);

        Node boutonOk = dialog.getDialogPane().lookupButton(btnValider);
        boutonOk.setDisable(true);

        // ─── Grille des fauteuils ───
        GridPane grille = new GridPane();
        grille.setHgap(5);
        grille.setVgap(5);
        grille.setAlignment(Pos.CENTER);

        int nbRangees = (int) Math.ceil(capacite / (double) PLACES_PAR_RANGEE);
        for (int r = 0; r < nbRangees; r++) {
            String lettre = lettreRangee(r);

            Label etiquette = new Label(lettre);
            etiquette.setMinWidth(24);
            etiquette.setStyle("-fx-font-weight: bold;");
            grille.add(etiquette, 0, r);

            for (int n = 1; n <= PLACES_PAR_RANGEE; n++) {
                int index = r * PLACES_PAR_RANGEE + n;
                if (index > capacite) break; // dernière rangée incomplète

                String numero = lettre + n;
                ToggleButton siege = new ToggleButton(String.valueOf(n));
                siege.setPrefSize(36, 30);
                siege.setMinSize(36, 30);

                if (occupees.contains(numero)) {
                    siege.setDisable(true);
                    siege.setStyle(STYLE_OCCUPE);
                } else {
                    siege.setStyle(STYLE_LIBRE);
                    siege.setOnAction(e -> {
                        if (siege.isSelected()) {
                            choisies.add(numero);
                            siege.setStyle(STYLE_SELECTIONNE);
                        } else {
                            choisies.remove(numero);
                            siege.setStyle(STYLE_LIBRE);
                        }
                        boutonOk.setDisable(choisies.isEmpty());
                        labelSelection.setText(choisies.isEmpty()
                                ? "Aucune place sélectionnée"
                                : choisies.size() + " place(s) : " + String.join(", ", choisies));
                    });
                }

                // décalage d'une colonne après la moitié pour créer l'allée centrale
                int colonne = n <= COLONNE_ALLEE ? n : n + 1;
                grille.add(siege, colonne, r);
            }
        }

        ScrollPane defilement = new ScrollPane(grille);
        defilement.setFitToWidth(true);
        defilement.setPrefViewportHeight(Math.min(380, nbRangees * 35 + 20));
        defilement.setStyle("-fx-background-color: transparent;");

        // ─── Écran ───
        Label ecran = new Label("É C R A N");
        ecran.setMaxWidth(Double.MAX_VALUE);
        ecran.setAlignment(Pos.CENTER);
        ecran.setStyle("-fx-background-color: #555; -fx-text-fill: white; "
                + "-fx-padding: 6; -fx-background-radius: 4 4 20 20; -fx-font-weight: bold;");

        // ─── Légende ───
        HBox legende = new HBox(14,
                pastille("#d9d9d9", "Libre"),
                pastille("#28a745", "Sélectionné"),
                pastille("#dc143c", "Réservé"));
        legende.setAlignment(Pos.CENTER);

        VBox contenu = new VBox(12, ecran, defilement, legende, labelSelection);
        contenu.setPadding(new Insets(10));
        contenu.setPrefWidth(520);
        dialog.getDialogPane().setContent(contenu);

        boolean valide = dialog.showAndWait().filter(b -> b == btnValider).isPresent();
        return valide ? new ArrayList<>(choisies) : new ArrayList<>();
    }

    private static HBox pastille(String couleur, String texte) {
        Region carre = new Region();
        carre.setPrefSize(14, 14);
        carre.setMinSize(14, 14);
        carre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 3;");
        HBox box = new HBox(5, carre, new Label(texte));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** 0 → A, 1 → B ... 25 → Z, 26 → AA ... */
    private static String lettreRangee(int index) {
        StringBuilder sb = new StringBuilder();
        int i = index;
        do {
            sb.insert(0, (char) ('A' + (i % 26)));
            i = i / 26 - 1;
        } while (i >= 0);
        return sb.toString();
    }

    /** Tri naturel : A1, A2, A10, B1 ... */
    private static int comparerPlaces(String a, String b) {
        String ra = a.replaceAll("\\d", "");
        String rb = b.replaceAll("\\d", "");
        if (ra.length() != rb.length()) return Integer.compare(ra.length(), rb.length());
        int c = ra.compareTo(rb);
        if (c != 0) return c;
        return Integer.compare(Integer.parseInt(a.replaceAll("\\D", "")),
                Integer.parseInt(b.replaceAll("\\D", "")));
    }
}
