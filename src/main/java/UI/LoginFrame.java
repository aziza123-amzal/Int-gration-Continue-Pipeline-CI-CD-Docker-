package UI;

import BD.Database;
import SERVER.ChatServer;
import UTILS.Hashage;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

/**
 * LoginFrame - Ecran de connexion Najma Chat.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Najma Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(860, 520);
        setLocationRelativeTo(null);
        new Thread(() -> ChatServer.main(new String[]{})).start();
        build();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());

        // ---- Panneau gauche dégradé ----
        JPanel left = NajmaTheme.makeGradientPanel();
        left.setPreferredSize(new Dimension(380, 520));
        left.setLayout(new GridBagLayout());

        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("✦", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        logo.setForeground(new Color(255,255,255,180));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Najma Chat", SwingConstants.CENTER);
        appName.setFont(new Font("Georgia", Font.BOLD, 34));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Connectez-vous avec vos proches", SwingConstants.CENTER);
        tagline.setFont(new Font("Georgia", Font.ITALIC, 13));
        tagline.setForeground(new Color(255, 220, 235));
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        brandBox.add(logo);
        brandBox.add(Box.createVerticalStrut(12));
        brandBox.add(appName);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(tagline);
        left.add(brandBox);

        // ---- Panneau droit formulaire ----
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(NajmaTheme.ROSE_LIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1;

        // Titre
        JLabel titre = new JLabel("Connexion", SwingConstants.CENTER);
        titre.setFont(NajmaTheme.FONT_TITLE);
        titre.setForeground(NajmaTheme.ROSE_DARK);
        g.gridy = 0; g.insets = new Insets(0, 0, 30, 0);
        right.add(titre, g);

        // Username
        JLabel lUser = new JLabel("Nom d'utilisateur");
        lUser.setFont(NajmaTheme.FONT_BOLD);
        lUser.setForeground(NajmaTheme.TEXT_DARK);
        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        right.add(lUser, g);

        usernameField = NajmaTheme.makeTextField();
        g.gridy = 2; g.insets = new Insets(0, 0, 16, 0);
        right.add(usernameField, g);

        // Password
        JLabel lPass = new JLabel("Mot de passe");
        lPass.setFont(NajmaTheme.FONT_BOLD);
        lPass.setForeground(NajmaTheme.TEXT_DARK);
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        right.add(lPass, g);

        passwordField = NajmaTheme.makePasswordField();
        g.gridy = 4; g.insets = new Insets(0, 0, 24, 0);
        right.add(passwordField, g);

        // Bouton
        JButton btnLogin = NajmaTheme.makeButton("Se connecter");
        g.gridy = 5; g.insets = new Insets(0, 0, 16, 0);
        right.add(btnLogin, g);

        // Lien inscription
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel lq = new JLabel("Pas de compte ?");
        lq.setFont(NajmaTheme.FONT_SMALL);
        lq.setForeground(NajmaTheme.TEXT_GRAY);
        JLabel lins = new JLabel("S'inscrire");
        lins.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lins.setForeground(NajmaTheme.ROSE_DARK);
        lins.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkRow.add(lq); linkRow.add(lins);
        g.gridy = 6; g.insets = new Insets(0, 0, 0, 0);
        right.add(linkRow, g);

        root.add(left,  BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        // Listeners
        btnLogin.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // MouseAdapter (classe Adapter - barème)
        lins.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new RegisterFrame();
            }
            @Override public void mouseEntered(MouseEvent e) {
                lins.setForeground(NajmaTheme.ROSE_DEEP);
            }
            @Override public void mouseExited(MouseEvent e) {
                lins.setForeground(NajmaTheme.ROSE_DARK);
            }
        });
    }

    private void login() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez remplir tous les champs.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "SELECT password FROM users WHERE username = ?")) {
            s.setString(1, user);
            ResultSet rs = s.executeQuery();
            if (rs.next() && Hashage.comparePassword(pass, rs.getString("password"))) {
                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE users SET status='online' WHERE username=?")) {
                    upd.setString(1, user); upd.executeUpdate();
                }
                dispose();
                new Home(user);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Identifiants incorrects.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur base de données :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Classe anonyme (barème)
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { new LoginFrame(); }
        });
    }
}
