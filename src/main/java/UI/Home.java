package UI;

import BD.Database;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;

/**
 * Home - Ecran principal Najma Chat.
 */
public class Home extends JFrame {

    private final String currentUser;
    private DefaultListModel<String> onlineModel   = new DefaultListModel<>();
    private DefaultListModel<String> allModel      = new DefaultListModel<>();
    private DefaultListModel<String> groupModel    = new DefaultListModel<>();
    private DefaultListModel<String> notifModel    = new DefaultListModel<>();
    private JList<String> onlineList, allList, groupList, notifList;
    private JLabel onlineBadge, notifBadge;
    private HashMap<String, ChatClient>      openChats  = new HashMap<>();
    private HashMap<String, ChatGroupClient> openGroups = new HashMap<>();

    public Home(String currentUser) {
        this.currentUser = currentUser;
        setTitle("Najma Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);
        build();
        loadAll();
        startTimers();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(NajmaTheme.BG_CHAT);

        // ======= SIDEBAR GAUCHE =======
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setBackground(NajmaTheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(270, getHeight()));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, NajmaTheme.BORDER));

        // Header sidebar
        JPanel sHeader = new JPanel(new BorderLayout(12, 0));
        sHeader.setBackground(NajmaTheme.ROSE_DARK);
        sHeader.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel myAvatar = NajmaTheme.makeAvatar(currentUser, 42);
        JPanel myInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        myInfo.setOpaque(false);
        JLabel myName = new JLabel(currentUser);
        myName.setFont(NajmaTheme.FONT_BOLD);
        myName.setForeground(Color.WHITE);
        onlineBadge = new JLabel("● 0 en ligne");
        onlineBadge.setFont(NajmaTheme.FONT_SMALL);
        onlineBadge.setForeground(new Color(255, 210, 230));
        myInfo.add(myName); myInfo.add(onlineBadge);
        sHeader.add(myAvatar, BorderLayout.WEST);
        sHeader.add(myInfo, BorderLayout.CENTER);
        sidebar.add(sHeader, BorderLayout.NORTH);

        // Contenu sidebar avec onglets
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(NajmaTheme.FONT_SMALL);
        tabs.setBackground(NajmaTheme.BG_SIDEBAR);

        onlineList = makeList(onlineModel, true, false);
        allList    = makeList(allModel, true, true);
        groupList  = makeList(groupModel, false, false);

        tabs.addTab("En ligne", new JScrollPane(onlineList) {{ setBorder(null); }});
        tabs.addTab("Tous", new JScrollPane(allList)    {{ setBorder(null); }});
        tabs.addTab("Groupes", new JScrollPane(groupList)  {{ setBorder(null); }});
        sidebar.add(tabs, BorderLayout.CENTER);

        // ======= ZONE CENTRE =======
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(NajmaTheme.BG_CHAT);

        // Header centre
        JPanel cHeader = new JPanel(new BorderLayout(0, 0));
        cHeader.setBackground(NajmaTheme.ROSE_DARK);
        cHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("Najma Chat  ✦");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        cHeader.add(title, BorderLayout.WEST);
        center.add(cHeader, BorderLayout.NORTH);

        // Page d'accueil vide
        JPanel welcome = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(NajmaTheme.BG_CHAT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(248, 187, 208, 50));
                g2.fillOval(getWidth()/2-120, 60, 240, 240);
                g2.dispose();
            }
        };
        GridBagConstraints wg = new GridBagConstraints();
        wg.gridx = 0;
        JLabel wIcon = new JLabel("✦");
        wIcon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        wIcon.setForeground(NajmaTheme.ROSE_MEDIUM);
        wg.gridy = 0; welcome.add(wIcon, wg);
        JLabel wText = new JLabel("Sélectionnez une conversation");
        wText.setFont(new Font("Georgia", Font.ITALIC, 17));
        wText.setForeground(NajmaTheme.TEXT_GRAY);
        wg.gridy = 1; wg.insets = new Insets(12,0,0,0);
        welcome.add(wText, wg);
        center.add(welcome, BorderLayout.CENTER);

        // ======= PANNEAU DROIT =======
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(NajmaTheme.BG_SIDEBAR);
        right.setPreferredSize(new Dimension(210, getHeight()));
        right.setBorder(new MatteBorder(0, 1, 0, 0, NajmaTheme.BORDER));

        // Boutons
        JButton bGroupe  = NajmaTheme.makeButton("+ Groupe");
        JButton bRefresh = NajmaTheme.makeButton("↻ Rafraîchir");
        JButton bLogout  = NajmaTheme.makeButton("Déconnexion");
        bGroupe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));
        btns.setOpaque(false);
        btns.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));
        btns.add(bGroupe);
        btns.add(Box.createVerticalStrut(8));
        btns.add(bRefresh);
        btns.add(Box.createVerticalStrut(8));
        btns.add(bLogout);
        right.add(btns);

        // Séparateur
        right.add(makeSep());

        // Notifications
        notifBadge = new JLabel("  🔔  0 notification");
        notifBadge.setFont(NajmaTheme.FONT_BOLD);
        notifBadge.setForeground(NajmaTheme.ROSE_DARK);
        notifBadge.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        notifBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        right.add(notifBadge);

        notifList = new JList<>(notifModel);
        notifList.setFont(NajmaTheme.FONT_SMALL);
        notifList.setBackground(NajmaTheme.BG_SIDEBAR);
        notifList.setForeground(NajmaTheme.TEXT_DARK);
        notifList.setFixedCellHeight(38);
        JScrollPane ns = new JScrollPane(notifList);
        ns.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        ns.setAlignmentX(LEFT_ALIGNMENT);
        right.add(ns);

        // Assemblage
        root.add(sidebar, BorderLayout.WEST);
        root.add(center,  BorderLayout.CENTER);
        root.add(right,   BorderLayout.EAST);
        setContentPane(root);

        // Listeners
        onlineList.addMouseListener(new MouseAdapter() {
            // Classe anonyme (barème)
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openChat(onlineList.getSelectedValue());
            }
        });
        allList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openChat(allList.getSelectedValue());
            }
        });
        groupList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openGroup(groupList.getSelectedValue());
            }
        });
        notifList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = notifList.locationToIndex(e.getPoint());
                if (i >= 0) markRead(i);
            }
        });
        bGroupe.addActionListener(e -> new CreateGroupFrame());
        bRefresh.addActionListener(e -> loadAll());
        bLogout.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                // Classe interne locale (barème)
                class Logout {
                    void run() {
                        Database.setUserOffline(currentUser);
                        dispose();
                        new LoginFrame();
                    }
                }
                new Logout().run();
            }
        });
    }

    // ---- Helpers UI ----

    private JList<String> makeList(DefaultListModel<String> model, boolean withAvatar, boolean withStatus) {
        JList<String> list = new JList<>(model);
        list.setBackground(NajmaTheme.BG_SIDEBAR);
        list.setFixedCellHeight(54);
        list.setSelectionBackground(NajmaTheme.ROSE_MEDIUM);
        list.setSelectionForeground(NajmaTheme.TEXT_DARK);
        if (withAvatar && withStatus) {
            list.setCellRenderer(new StatusRenderer());
        } else if (withAvatar) {
            list.setCellRenderer(new AvatarRenderer());
        }
        return list;
    }

    private JSeparator makeSep() {
        JSeparator s = new JSeparator();
        s.setForeground(NajmaTheme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private void openChat(String user) {
        if (user == null || user.equals(currentUser)) return;
        if (!openChats.containsKey(user)) {
            openChats.put(user, new ChatClient("localhost", currentUser, user));
        } else {
            JOptionPane.showMessageDialog(this,
                "La conversation avec " + user + " est déjà ouverte.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openGroup(String gname) {
        if (gname == null) return;
        int gid = Database.getGroupIdFromName(gname);
        if (!openGroups.containsKey(gname)) {
            openGroups.put(gname, new ChatGroupClient("localhost", currentUser, gid));
        } else {
            JOptionPane.showMessageDialog(this,
                "La discussion " + gname + " est déjà ouverte.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---- Données ----

    private void loadAll() {
        fetchOnline(); fetchAll(); fetchGroups(); fetchNotifs();
    }

    private void fetchOnline() {
        onlineModel.clear();
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT username FROM users WHERE status='online'");
             ResultSet r = s.executeQuery()) {
            int n = 0;
            while (r.next()) {
                String u = r.getString("username");
                if (!u.equals(currentUser)) onlineModel.addElement(u);
                n++;
            }
            onlineBadge.setText("● " + n + " en ligne");
        } catch (SQLException e) { System.err.println("fetchOnline: " + e.getMessage()); }
    }

    private void fetchAll() {
        allModel.clear();
        String q = "SELECT u.username, u.status FROM users u "
                 + "LEFT JOIN group_members gm ON u.id=gm.user_id "
                 + "GROUP BY u.id ORDER BY u.status DESC, u.username";
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(q);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                String u = r.getString("username");
                if (!u.equals(currentUser)) allModel.addElement(u);
            }
        } catch (SQLException e) { System.err.println("fetchAll: " + e.getMessage()); }
    }

    private void fetchGroups() {
        groupModel.clear();
        String q = "SELECT cg.name FROM chat_groups cg "
                 + "JOIN group_members gm ON cg.id=gm.group_id "
                 + "JOIN users u ON gm.user_id=u.id WHERE u.username=?";
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(q)) {
            s.setString(1, currentUser);
            ResultSet r = s.executeQuery();
            while (r.next()) groupModel.addElement(r.getString("name"));
        } catch (SQLException e) { System.err.println("fetchGroups: " + e.getMessage()); }
    }

    private void fetchNotifs() {
        notifModel.clear();
        String q = "SELECT s.username sender, m.content, n.group_message, cg.name grp "
                 + "FROM notifications n "
                 + "JOIN messages m ON n.message_id=m.id "
                 + "JOIN users s ON n.sender_id=s.id "
                 + "LEFT JOIN chat_groups cg ON m.group_id=cg.id "
                 + "WHERE n.user_id=(SELECT id FROM users WHERE username=?) AND n.seen=false";
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(q)) {
            s.setString(1, currentUser);
            ResultSet r = s.executeQuery();
            int n = 0;
            while (r.next()) {
                String sender = r.getString("sender");
                String msg    = r.getString("content");
                boolean isGrp = r.getBoolean("group_message");
                String grp    = r.getString("grp");
                notifModel.addElement(isGrp && grp != null
                    ? "[" + grp + "] " + sender + ": " + msg
                    : sender + ": " + msg);
                n++;
            }
            notifBadge.setText("  🔔  " + n + (n>1?" notifications":" notification"));
            notifBadge.setForeground(n > 0 ? NajmaTheme.ROSE_DEEP : NajmaTheme.TEXT_GRAY);
        } catch (SQLException e) { System.err.println("fetchNotifs: " + e.getMessage()); }
    }

    private void markRead(int index) {
        try (Connection c = Database.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE notifications SET seen=true WHERE id=(" +
                 "SELECT id FROM (SELECT n.id FROM notifications n " +
                 "JOIN messages m ON n.message_id=m.id " +
                 "WHERE n.user_id=(SELECT id FROM users WHERE username=?) " +
                 "AND n.seen=false LIMIT 1) tmp)")) {
            s.setString(1, currentUser);
            s.executeUpdate();
        } catch (SQLException e) { System.err.println("markRead: " + e.getMessage()); }
        notifModel.remove(index);
        notifBadge.setText("  🔔  " + notifModel.size() + " notification" + (notifModel.size()>1?"s":""));
    }

    private void startTimers() {
        new Timer(2000, e -> fetchOnline()).start();
        new Timer(2500, e -> fetchAll()).start();
        new Timer(3000, e -> fetchGroups()).start();
        new Timer(2000, e -> fetchNotifs()).start();
    }

    // ---- Renderers ----

    class AvatarRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                int i, boolean sel, boolean foc) {
            String name = (String) v;
            JPanel p = new JPanel(new BorderLayout(10, 0));
            p.setBackground(sel ? NajmaTheme.ROSE_MEDIUM : NajmaTheme.BG_SIDEBAR);
            p.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            p.add(NajmaTheme.makeAvatar(name, 38), BorderLayout.WEST);
            JLabel nm = new JLabel(name);
            nm.setFont(NajmaTheme.FONT_BOLD);
            nm.setForeground(NajmaTheme.TEXT_DARK);
            p.add(nm, BorderLayout.CENTER);
            return p;
        }
    }

    class StatusRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                int i, boolean sel, boolean foc) {
            String name   = (String) v;
            String status = Database.getUserStatus(name);
            boolean on    = "online".equals(status);
            JPanel p = new JPanel(new BorderLayout(10, 0));
            p.setBackground(sel ? NajmaTheme.ROSE_MEDIUM : NajmaTheme.BG_SIDEBAR);
            p.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            p.add(NajmaTheme.makeAvatar(name, 38), BorderLayout.WEST);
            JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
            info.setOpaque(false);
            JLabel nm = new JLabel(name);
            nm.setFont(NajmaTheme.FONT_BOLD);
            nm.setForeground(NajmaTheme.TEXT_DARK);
            JLabel st = new JLabel(on ? "● En ligne" : "● Hors ligne");
            st.setFont(NajmaTheme.FONT_SMALL);
            st.setForeground(on ? NajmaTheme.ONLINE : NajmaTheme.OFFLINE);
            info.add(nm); info.add(st);
            p.add(info, BorderLayout.CENTER);
            return p;
        }
    }
}
