package UI;

import BD.Database;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ChatGroupClient {

    private JFrame frame;
    private JTextField messageField;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private int groupId;
    private List<String> members;

    public ChatGroupClient(String host, String me, int groupId) {
        this.username = me;
        this.groupId  = groupId;
        members = Database.getGroupMembers(groupId);
        if (!members.contains(me)) {
            JOptionPane.showMessageDialog(null, "Vous n'êtes pas membre de ce groupe.", "Refus", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String gname = Database.getGroupNameFromId(groupId);
        frame = new JFrame("Najma Chat — " + gname);
        frame.setSize(540, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        build(gname);
        connect(host);
        loadHistory();
        frame.setVisible(true);
    }

    private void build(String gname) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NajmaTheme.BG_CHAT);

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(NajmaTheme.ROSE_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel av = NajmaTheme.makeAvatar(gname, 42);
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel nm = new JLabel(gname);
        nm.setFont(NajmaTheme.FONT_BOLD);
        nm.setForeground(Color.WHITE);
        JLabel ms = new JLabel(members.size() + " membres");
        ms.setFont(NajmaTheme.FONT_SMALL);
        ms.setForeground(new Color(255, 210, 230));
        info.add(nm); info.add(ms);
        header.add(av, BorderLayout.WEST);
        header.add(info, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Chat
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(NajmaTheme.BG_CHAT);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(NajmaTheme.BG_CHAT);
        root.add(chatScroll, BorderLayout.CENTER);

        // Input
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, NajmaTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        messageField = NajmaTheme.makeTextField();
        JButton send = NajmaTheme.makeSendButton();
        inputBar.add(messageField, BorderLayout.CENTER);
        inputBar.add(send, BorderLayout.EAST);
        root.add(inputBar, BorderLayout.SOUTH);
        frame.setContentPane(root);

        send.addActionListener(e -> send());
        messageField.addActionListener(e -> send());
    }

    private void addBubble(String sender, String text) {
        boolean me = sender.equals(username);
        JPanel row = new JPanel(new FlowLayout(me ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        if (!me) row.add(NajmaTheme.makeAvatar(sender, 34));

        JPanel bubble = new JPanel(new BorderLayout(0, 3)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(me ? NajmaTheme.BUBBLE_ME : NajmaTheme.BUBBLE_OTHER);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                if (!me) { g2.setColor(NajmaTheme.BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); }
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        if (!me) {
            JLabel senderLbl = new JLabel(sender);
            senderLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            senderLbl.setForeground(NajmaTheme.ROSE_DEEP);
            bubble.add(senderLbl, BorderLayout.NORTH);
        }
        JLabel msg = new JLabel("<html><body style='max-width:195px'>" + text + "</body></html>");
        msg.setFont(NajmaTheme.FONT_REGULAR);
        msg.setForeground(NajmaTheme.TEXT_DARK);
        bubble.add(msg, BorderLayout.CENTER);
        bubble.setMaximumSize(new Dimension(300, 9999));

        row.add(bubble);
        if (me) row.add(NajmaTheme.makeAvatar(username, 34));

        chatPanel.add(row);
        chatPanel.add(Box.createVerticalStrut(3));
        chatPanel.revalidate();
        SwingUtilities.invokeLater(() -> {
            JScrollBar b = chatScroll.getVerticalScrollBar();
            b.setValue(b.getMaximum());
        });
    }

    private void loadHistory() {
        List<String> msgs = Database.getGroupChatHistory(groupId);
        SwingUtilities.invokeLater(() -> {
            for (String m : msgs) {
                String[] p = m.split(": ", 2);
                if (p.length == 2) addBubble(p[0].trim(), p[1].trim());
            }
        });
    }

    private void connect(String host) {
        try {
            Socket s = new Socket(host, 12345);
            in  = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            out.println("GROUP:" + groupId + ":" + username);
            new Thread(this::listen).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connexion impossible.\n" + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void send() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        if (!members.contains(username)) {
            JOptionPane.showMessageDialog(frame, "Vous n'êtes plus membre.", "Refus", JOptionPane.ERROR_MESSAGE);
            return;
        }
        out.println("GROUP:" + groupId + ":" + username + ":" + text);
        SwingUtilities.invokeLater(() -> addBubble(username, text));
        messageField.setText("");
        Database.saveGroupMessage(username, groupId, text);
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("GROUP:" + groupId + ":")) {
                    String[] p = line.split(":", 4);
                    if (p.length == 4 && !p[2].trim().equals(username)) {
                        final String s = p[2].trim(), c = p[3].trim();
                        SwingUtilities.invokeLater(() -> addBubble(s, c));
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connexion perdue.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
