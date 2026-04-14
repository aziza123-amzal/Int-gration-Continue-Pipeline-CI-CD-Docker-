package BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Database - Gestion de la base de données MySQL pour ChatApp.
 * Fournit toutes les méthodes d'accès aux données (messages, utilisateurs,
 * groupes, notifications).
 *
 * BONUS implémentés :
 *  - Affichage du statut en ligne/hors ligne
 *  - Affichage des utilisateurs connectés
 *  - Utilisation de requêtes avec jointure
 *  - Messages d'erreur clairs en cas de problème
 *  - Gestion du thème personnalisé
 */
public class Database {

    private static final String URL  = "jdbc:mysql://localhost:3306/ChatApp?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    // =========================================================
    //  CONNEXION
    // =========================================================

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL introuvable ! Ajoutez mysql-connector.jar au classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // =========================================================
    //  UTILISATEURS
    // =========================================================

    /** Marque l'utilisateur comme offline. */
    public static void setUserOffline(String username) {
        String query = "UPDATE users SET status = 'offline' WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur setUserOffline (" + username + ") : " + e.getMessage());
        }
    }

    /** Marque l'utilisateur comme online. */
    public static void setUserOnline(String username) {
        String query = "UPDATE users SET status = 'online' WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur setUserOnline (" + username + ") : " + e.getMessage());
        }
    }

    /**
     * BONUS - Retourne le statut d'un utilisateur ("online"/"offline"/"inconnu").
     */
    public static String getUserStatus(String username) {
        String query = "SELECT status FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (SQLException e) {
            System.err.println("Erreur getUserStatus : " + e.getMessage());
        }
        return "inconnu";
    }

    /**
     * BONUS - Liste tous les utilisateurs avec leur statut (requete avec LEFT JOIN).
     * @return liste "username [online]" ou "username [offline]"
     */
    public static List<String> getAllUsersWithStatus() {
        List<String> result = new ArrayList<>();
        String query = "SELECT u.username, u.status "
                     + "FROM users u "
                     + "LEFT JOIN group_members gm ON u.id = gm.user_id "
                     + "GROUP BY u.id, u.username, u.status "
                     + "ORDER BY u.status DESC, u.username ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String user   = rs.getString("username");
                String status = rs.getString("status");
                String badge  = "online".equals(status) ? " [en ligne]" : " [hors ligne]";
                result.add(user + badge);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllUsersWithStatus : " + e.getMessage());
        }
        return result;
    }

    /**
     * BONUS - Sauvegarde le thème de l'utilisateur (light/dark).
     */
    public static void saveUserTheme(String username, String theme) {
        String query = "UPDATE users SET theme = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, theme);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur saveUserTheme : " + e.getMessage());
        }
    }

    /**
     * BONUS - Recupere le theme de l'utilisateur.
     */
    public static String getUserTheme(String username) {
        String query = "SELECT theme FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("theme");
        } catch (SQLException e) {
            System.err.println("Erreur getUserTheme : " + e.getMessage());
        }
        return "light";
    }

    // =========================================================
    //  MESSAGES PRIVES
    // =========================================================

    /** Enregistre un message prive dans la base. */
    public static void saveMessageToDatabase(String sender, String receiver, String content) {
        String query = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) "
                     + "VALUES ((SELECT id FROM users WHERE username = ?), "
                     + "        (SELECT id FROM users WHERE username = ?), ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur saveMessage : " + e.getMessage());
        }
    }

    /**
     * Historique de conversation prive (requete avec double JOIN sur users).
     */
    public static List<String> getChatHistory(String user1, String user2) {
        List<String> chatHistory = new ArrayList<>();
        String query = "SELECT u_sender.username AS sender, m.content "
                     + "FROM messages m "
                     + "JOIN users u_sender ON m.sender_id   = u_sender.id "
                     + "JOIN users u_recv   ON m.receiver_id = u_recv.id "
                     + "WHERE (u_sender.username = ? AND u_recv.username = ?) "
                     + "   OR (u_sender.username = ? AND u_recv.username = ?) "
                     + "ORDER BY m.timestamp ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user1); stmt.setString(2, user2);
            stmt.setString(3, user2); stmt.setString(4, user1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chatHistory.add(rs.getString("sender") + ": " + rs.getString("content"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getChatHistory : " + e.getMessage());
        }
        return chatHistory;
    }

    // =========================================================
    //  MESSAGES DE GROUPE
    // =========================================================

    /** Enregistre un message de groupe dans la base. */
    public static void saveGroupMessage(String sender, int groupId, String content) {
        String query = "INSERT INTO messages (sender_id, group_id, content, timestamp) "
                     + "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setInt(2, groupId);
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur saveGroupMessage : " + e.getMessage());
        }
    }

    /**
     * Historique de groupe (requete avec JOIN sur users + chat_groups).
     */
    public static List<String> getGroupChatHistory(int groupId) {
        List<String> chatHistory = new ArrayList<>();
        String query = "SELECT u.username AS sender, m.content "
                     + "FROM messages m "
                     + "JOIN users       u  ON m.sender_id = u.id "
                     + "JOIN chat_groups cg ON m.group_id  = cg.id "
                     + "WHERE m.group_id = ? "
                     + "ORDER BY m.timestamp ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chatHistory.add(rs.getString("sender") + ": " + rs.getString("content"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getGroupChatHistory : " + e.getMessage());
        }
        return chatHistory;
    }

    // =========================================================
    //  GROUPES
    // =========================================================

    /** Membres d'un groupe (JOIN sur users + group_members). */
    public static List<String> getGroupMembers(int groupId) {
        List<String> members = new ArrayList<>();
        String query = "SELECT u.username "
                     + "FROM users u "
                     + "JOIN group_members gm ON u.id = gm.user_id "
                     + "WHERE gm.group_id = ? ORDER BY u.username";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) members.add(rs.getString("username"));
        } catch (SQLException e) {
            System.err.println("Erreur getGroupMembers : " + e.getMessage());
        }
        return members;
    }

    public static int getGroupIdFromName(String groupName) {
        String query = "SELECT id FROM chat_groups WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Erreur getGroupIdFromName : " + e.getMessage());
        }
        return -1;
    }

    public static String getGroupNameFromId(int groupId) {
        String query = "SELECT name FROM chat_groups WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            System.err.println("Erreur getGroupNameFromId : " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    //  NOTIFICATIONS (BONUS)
    // =========================================================

    /** Enregistre une notification pour un message prive. */
    public static void storeNotification(String sender, String recipient, String content) {
        String query = "INSERT INTO notifications (message_id, user_id, sender_id, seen) "
                     + "VALUES ((SELECT id FROM messages WHERE content = ? ORDER BY timestamp DESC LIMIT 1), "
                     + "        (SELECT id FROM users WHERE username = ?), "
                     + "        (SELECT id FROM users WHERE username = ?), false)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, content);
            stmt.setString(2, recipient);
            stmt.setString(3, sender);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur storeNotification : " + e.getMessage());
        }
    }

    /** Enregistre une notification pour un message de groupe. */
    public static void storeNotificationGroup(String sender, String recipient, String content) {
        String query = "INSERT INTO notifications (message_id, user_id, sender_id, seen, group_message) "
                     + "VALUES ((SELECT id FROM messages WHERE content = ? ORDER BY timestamp DESC LIMIT 1), "
                     + "        (SELECT id FROM users WHERE username = ?), "
                     + "        (SELECT id FROM users WHERE username = ?), false, true)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, content);
            stmt.setString(2, recipient);
            stmt.setString(3, sender);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur storeNotificationGroup : " + e.getMessage());
        }
    }

    /** @deprecated Utiliser storeNotification() */
    @Deprecated
    public static void saveNotification(String receiver, int messageId) {
        String query = "INSERT INTO notifications (message_id, user_id, seen) "
                     + "VALUES (?, (SELECT id FROM users WHERE username = ?), false)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, messageId);
            stmt.setString(2, receiver);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur saveNotification : " + e.getMessage());
        }
    }
}