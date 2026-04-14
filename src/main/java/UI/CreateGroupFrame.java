package UI;

import BD.Database;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class CreateGroupFrame extends JFrame {

    private JTextField groupNameField;
    private JList<String> userList;
    private DefaultListModel<String> userModel;

    public CreateGroupFrame() {
        setTitle("Najma Chat — Nouveau groupe");
        setSize(440, 560);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        build();
        loadUsers();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NajmaTheme.ROSE_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NajmaTheme.ROSE_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Créer un groupe");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(NajmaTheme.ROSE_LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel lName = new JLabel("Nom du groupe");
        lName.setFont(NajmaTheme.FONT_BOLD);
        lName.setForeground(NajmaTheme.TEXT_DARK);
        lName.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lName);
        form.add(Box.createVerticalStrut(8));

        groupNameField = NajmaTheme.makeTextField();
        groupNameField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(groupNameField);
        form.add(Box.createVerticalStrut(20));

        JLabel lMembers = new JLabel("Membres  (Ctrl + clic pour sélectionner plusieurs)");
        lMembers.setFont(NajmaTheme.FONT_SMALL);
        lMembers.setForeground(NajmaTheme.TEXT_GRAY);
        lMembers.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lMembers);
        form.add(Box.createVerticalStrut(8));

        userModel = new DefaultListModel<>();
        userList  = new JList<>(userModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        userList.setBackground(Color.WHITE);
        userList.setFixedCellHeight(48);
        userList.setSelectionBackground(NajmaTheme.ROSE_MEDIUM);
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                    int i, boolean sel, boolean foc) {
                JPanel p = new JPanel(new BorderLayout(10, 0));
                p.setBackground(sel ? NajmaTheme.ROSE_MEDIUM : Color.WHITE);
                p.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
                p.add(NajmaTheme.makeAvatar((String)v, 36), BorderLayout.WEST);
                JLabel nm = new JLabel((String)v);
                nm.setFont(NajmaTheme.FONT_BOLD);
                nm.setForeground(NajmaTheme.TEXT_DARK);
                p.add(nm, BorderLayout.CENTER);
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(userList);
        scroll.setBorder(BorderFactory.createLineBorder(NajmaTheme.BORDER));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        form.add(scroll);
        form.add(Box.createVerticalStrut(20));

        JButton btn = NajmaTheme.makeButton("Créer le groupe");
        btn.setAlignmentX(CENTER_ALIGNMENT);
        form.add(btn);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);

        btn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { createGroup(); }
        });
    }

    private void loadUsers() {
        SwingUtilities.invokeLater(() -> {
            try (Connection c = Database.getConnection();
                 PreparedStatement s = c.prepareStatement("SELECT username FROM users ORDER BY username");
                 ResultSet r = s.executeQuery()) {
                userModel.clear();
                while (r.next()) userModel.addElement(r.getString("username"));
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur chargement utilisateurs :\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createGroup() {
        String name = groupNameField.getText().trim();
        List<String> sel = userList.getSelectedValuesList();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Entrez un nom de groupe.", "Champ vide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sélectionnez au moins un membre.", "Sélection vide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = Database.getConnection()) {
            PreparedStatement s = c.prepareStatement(
                "INSERT INTO chat_groups (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            s.setString(1, name); s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            int gid = r.next() ? r.getInt(1) : -1;

            PreparedStatement ms = c.prepareStatement(
                "INSERT INTO group_members (user_id, group_id, role) VALUES ((SELECT id FROM users WHERE username=?),?,'member')");
            for (String u : sel) { ms.setString(1,u); ms.setInt(2,gid); ms.addBatch(); }
            ms.executeBatch();

            JOptionPane.showMessageDialog(this,
                "Groupe \"" + name + "\" créé avec " + sel.size() + " membres !", "Succès ✦", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur création groupe :\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
