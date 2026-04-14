package UI;

import BD.Database;
import UTILS.Hashage;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

/**
 * RegisterFrame - Ecran d'inscription Najma Chat.
 */
public class RegisterFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;

    public RegisterFrame() {
        setTitle("Najma Chat — Inscription");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(860, 520);
        setLocationRelativeTo(null);
        build();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());

        // Panneau gauche
        JPanel left = NajmaTheme.makeGradientPanel();
        left.setPreferredSize(new Dimension(380, 520));
        left.setLayout(new GridBagLayout());

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("✦", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        logo.setForeground(new Color(255,255,255,160));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel("Najma Chat", SwingConstants.CENTER);
        name.setFont(new Font("Georgia", Font.BOLD, 34));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Créez votre compte", SwingConstants.CENTER);
        sub.setFont(new Font("Georgia", Font.ITALIC, 13));
        sub.setForeground(new Color(255, 220, 235));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        brand.add(logo);
        brand.add(Box.createVerticalStrut(12));
        brand.add(name);
        brand.add(Box.createVerticalStrut(8));
        brand.add(sub);
        left.add(brand);

        // Panneau droit
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(NajmaTheme.ROSE_LIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1;

        JLabel titre = new JLabel("Inscription", SwingConstants.CENTER);
        titre.setFont(NajmaTheme.FONT_TITLE);
        titre.setForeground(NajmaTheme.ROSE_DARK);
        g.gridy = 0; g.insets = new Insets(0, 0, 30, 0);
        right.add(titre, g);

        JLabel lUser = new JLabel("Nom d'utilisateur");
        lUser.setFont(NajmaTheme.FONT_BOLD);
        lUser.setForeground(NajmaTheme.TEXT_DARK);
        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        right.add(lUser, g);

        usernameField = NajmaTheme.makeTextField();
        g.gridy = 2; g.insets = new Insets(0, 0, 16, 0);
        right.add(usernameField, g);

        JLabel lPass = new JLabel("Mot de passe");
        lPass.setFont(NajmaTheme.FONT_BOLD);
        lPass.setForeground(NajmaTheme.TEXT_DARK);
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        right.add(lPass, g);

        passwordField = NajmaTheme.makePasswordField();
        g.gridy = 4; g.insets = new Insets(0, 0, 24, 0);
        right.add(passwordField, g);

        JButton btnReg = NajmaTheme.makeButton("Créer mon compte");
        g.gridy = 5; g.insets = new Insets(0, 0, 16, 0);
        right.add(btnReg, g);

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel lq = new JLabel("Déjà un compte ?");
        lq.setFont(NajmaTheme.FONT_SMALL);
        lq.setForeground(NajmaTheme.TEXT_GRAY);
        JLabel lcon = new JLabel("Se connecter");
        lcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lcon.setForeground(NajmaTheme.ROSE_DARK);
        lcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkRow.add(lq); linkRow.add(lcon);
        g.gridy = 6;
        right.add(linkRow, g);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        btnReg.addActionListener(e -> register());
        passwordField.addActionListener(e -> register());

        lcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose(); new LoginFrame();
            }
            @Override public void mouseEntered(MouseEvent e) { lcon.setForeground(NajmaTheme.ROSE_DEEP); }
            @Override public void mouseExited(MouseEvent e)  { lcon.setForeground(NajmaTheme.ROSE_DARK); }
        });
    }

    private void register() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Remplissez tous les champs.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Mot de passe trop court (min 4 caractères).", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO users (username, password, status) VALUES (?,?,'online')")) {
            s.setString(1, user);
            s.setString(2, Hashage.hashPassword(pass));
            s.executeUpdate();
            // BONUS : boîte de confirmation
            int ok = JOptionPane.showConfirmDialog(this,
                "Compte créé avec succès !\nAccéder à l'accueil ?",
                "Bienvenue sur Najma Chat ✦", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { dispose(); new Home(user); }
        } catch (SQLException ex) {
            String msg = ex.getMessage().contains("Duplicate")
                ? "Ce nom d'utilisateur est déjà pris."
                : "Erreur : " + ex.getMessage();
            JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
