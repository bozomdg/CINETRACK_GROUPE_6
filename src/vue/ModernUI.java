package vue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Outils pour avoir un style commun dans toute l'application
 * (couleurs, polices, boutons, champs...).
 */
public class ModernUI {

    // Couleurs
    public static final Color PRIMARY = new Color(220, 20, 60);
    public static final Color PRIMARY_DARK = new Color(176, 16, 48);
    public static final Color ACCENT = new Color(70, 130, 180);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color BG_LIGHT = new Color(248, 249, 250);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color TEXT_DARK = new Color(33, 37, 41);
    public static final Color TEXT_MUTED = new Color(108, 117, 125);
    public static final Color BORDER = new Color(222, 226, 230);

    // Polices
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /** Crée un bouton coloré */
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        return btn;
    }

    /** Crée un champ de texte avec une bordure grise et un petit espace intérieur */
    public static JTextField createTextField(int cols) {
        JTextField field = new JTextField(cols);
        field.setFont(FONT_BODY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    /** Crée un champ mot de passe (même style que le champ texte) */
    public static JPasswordField createPasswordField(int cols) {
        JPasswordField field = new JPasswordField(cols);
        field.setFont(FONT_BODY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    /** Crée une liste déroulante */
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(FONT_BODY);
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(BORDER));
        return box;
    }

    /** Crée un panneau blanc avec une bordure  */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        return card;
    }

    /** Applique le style à un tableau */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);

        // En-tête du tableau
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** Affiche une boîte de message */
    public static void showStyledMessage(Component parent, String message, String title, int type) {
        JOptionPane.showMessageDialog(parent, message, title, type);
    }

    /** Affiche un message d'erreur */
    public static void showError(Component parent, String message) {
        showStyledMessage(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    /** Affiche un message de succès */
    public static void showSuccess(Component parent, String message) {
        showStyledMessage(parent, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Affiche une confirmation Oui/Non */
    public static int showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation", JOptionPane.YES_NO_OPTION);
    }

    /** Panneau avec les coins arrondis */
    public static class RoundedPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bgColor = bg;
            setOpaque(false); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }
}