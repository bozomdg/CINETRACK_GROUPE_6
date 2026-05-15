package modele;

import java.sql.Timestamp;

/**
 * Représente une entrée de journal (log) suite à un ajout ou une suppression
 * de compte gestionnaire.
 */
public class LogGestionnaire {
    private int idLog;
    private String action; // 'AJOUT' ou 'SUPPRESSION'
    private String usernameGestionnaire;
    private int idGestionnaire;
    private Timestamp dateAction;

    public LogGestionnaire(int idLog, String action, String usernameGestionnaire, int idGestionnaire, Timestamp dateAction) {
        this.idLog = idLog;
        this.action = action;
        this.usernameGestionnaire = usernameGestionnaire;
        this.idGestionnaire = idGestionnaire;
        this.dateAction = dateAction;
    }

    public int getIdLog() {
        return idLog;
    }

    public String getAction() {
        return action;
    }

    public String getUsernameGestionnaire() {
        return usernameGestionnaire;
    }

    public int getIdGestionnaire() {
        return idGestionnaire;
    }

    public Timestamp getDateAction() {
        return dateAction;
    }
}
