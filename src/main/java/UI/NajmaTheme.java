package UI;

import java.awt.*;
import javax.swing.*;

/**
 * NajmaTheme - Palette de couleurs et composants visuels pour Najma Chat.
 * Style : WhatsApp rose poudré, moderne et doux.
 */
public class NajmaTheme {

    // Palette
    public static final Color ROSE_LIGHT   = new Color(255, 235, 238);
    public static final Color ROSE_MEDIUM  = new Color(248, 187, 208);
    public static final Color ROSE_DARK    = new Color(216, 102, 145);
    public static final Color ROSE_DEEP    = new Color(173, 52, 103);
    public static final Color BUBBLE_ME    = new Color(252, 210, 225);
    public static final Color BUBBLE_OTHER = new Color(255, 255, 255);
    public static final Color BG_CHAT      = new Color(253, 244, 248);
    public static final Color BG_SIDEBAR   = new Color(255, 248, 251);
    public static final Color TEXT_DARK    = new Color(45, 25, 35);
    public static final Color TEXT_GRAY    = new Color(160, 130, 145);
    public static final Color ONLINE  = new Color(75, 181, 67);   // vert vif
    public static final Color OFFLINE = new Color(190, 180, 185); // gris doux
    public static final Color WHITE        = Color.WHITE;
    public static final Color BORDER       = new Color(240, 210, 225);

    // Polices
    public static final Font FONT_TITLE   = new Font("Georgia", Font.BOLD, 30);
    public static final Font FONT_SUBTITLE= new Font("Georgia", Font.ITALIC, 14);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_AVATAR  = new Font("Segoe UI", Font.BOLD, 15);

    /** Bouton principal arrondi rose */
    public static JButton makeButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ROSE_DEEP
                         : getModel().isRollover() ? ROSE_DEEP
                         : ROSE_DARK;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(WHITE);
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Champ texte arrondi */
    public static JTextField makeTextField() {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 22, 22);
                g2.setColor(isFocusOwner() ? ROSE_DARK : ROSE_MEDIUM);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 22, 22);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        f.setFont(FONT_REGULAR);
        f.setForeground(TEXT_DARK);
        f.setPreferredSize(new Dimension(300, 46));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return f;
    }

    /** Champ mot de passe arrondi */
    public static JPasswordField makePasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 22, 22);
                g2.setColor(isFocusOwner() ? ROSE_DARK : ROSE_MEDIUM);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 22, 22);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        f.setFont(FONT_REGULAR);
        f.setForeground(TEXT_DARK);
        f.setPreferredSize(new Dimension(300, 46));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return f;
    }

    /** Avatar rond coloré avec initiales */
    public static JLabel makeAvatar(String name, int size) {
        Color[] palette = {
            new Color(216,102,145), new Color(147,112,219),
            new Color(100,181,246), new Color(77,182,172),
            new Color(255,167,38),  new Color(239,83,80),
            new Color(102,187,106), new Color(255,138,101)
        };
        Color color  = palette[Math.abs(name.hashCode()) % palette.length];
        String init  = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
        int fontSize = size / 2 - 2;

        JLabel av = new JLabel(init, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init,
                    (getWidth()  - fm.stringWidth(init)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        av.setPreferredSize(new Dimension(size, size));
        av.setMinimumSize(new Dimension(size, size));
        av.setMaximumSize(new Dimension(size, size));
        return av;
    }

    /** Panneau avec fond dégradé rose */
    public static JPanel makeGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, ROSE_DARK, 0, getHeight(), new Color(173, 52, 103));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Cercles décoratifs
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-50, -50, 220, 220);
                g2.fillOval(getWidth()-120, getHeight()-120, 220, 220);
                g2.fillOval(getWidth()/2-80, getHeight()/2-30, 160, 160);
                g2.dispose();
            }
        };
    }

    /** Bouton d'envoi compact */
    public static JButton makeSendButton() {
        JButton btn = new JButton("➤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ROSE_DEEP : ROSE_DARK);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("➤",
                    (getWidth()  - fm.stringWidth("➤")) / 2 - 1,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setMinimumSize(new Dimension(44, 44));
        btn.setMaximumSize(new Dimension(44, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
