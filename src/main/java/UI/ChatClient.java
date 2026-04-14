package UI;

import BD.Database;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * ChatClient - Chat privé avec bulles de messages Najma Chat.
 */
public class ChatClient {

    private JFrame   frame;
    private JTextField messageField;
    private JPanel   chatPanel;
    private JScrollPane chatScroll;
    private PrintWriter out;
    private BufferedReader in;
    private String username, target;

    public ChatClient(String host, String me, String target) {
        this.username = me;
        this.target   = target;
        frame = new JFrame("Najma Chat — " + target);
        frame.setSize(520, 640);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        build();
        connect(host);
        loadHistory();
        frame.setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NajmaTheme.BG_CHAT);

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(NajmaTheme.ROSE_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel av = NajmaTheme.makeAvatar(target, 42);
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel nm = new JLabel(target);
        nm.setFont(NajmaTheme.FONT_BOLD);
        nm.setForeground(Color.WHITE);
        String st = Database.getUserStatus(target);
        JLabel sl = new JLabel("online".equals(st) ? "● En ligne" : "● Hors ligne");
        sl.setFont(NajmaTheme.FONT_SMALL);
        sl.setForeground("online".equals(st) ? new Color(180,255,180) : new Color(255,200,215));
        info.add(nm); info.add(sl);
        header.add(av, BorderLayout.WEST);
        header.add(info, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Zone messages
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(NajmaTheme.BG_CHAT);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(NajmaTheme.BG_CHAT);
        root.add(chatScroll, BorderLayout.CENTER);

        // Zone saisie
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, NajmaTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        messageField = NajmaTheme.makeTextField();
        messageField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton send = NajmaTheme.makeSendButton();
        inputBar.add(messageField, BorderLayout.CENTER);
        inputBar.add(send, BorderLayout.EAST);
        root.add(inputBar, BorderLayout.SOUTH);
        frame.setContentPane(root);

        send.addActionListener(e -> send());
        messageField.addActionListener(e -> send());
    }

    private void addBubble(String sender, String text) {
        boolean me = sender.equals(username) || sender.equals("Moi");
        JPanel row = new JPanel(new FlowLayout(me ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        if (!me) row.add(NajmaTheme.makeAvatar(sender, 34));

        // Bulle
        JLabel msg = new JLabel("<html><body style='max-width:210px;padding:2px'>" + text + "</body></html>");
        msg.setFont(NajmaTheme.FONT_REGULAR);
        msg.setForeground(NajmaTheme.TEXT_DARK);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(me ? NajmaTheme.BUBBLE_ME : NajmaTheme.BUBBLE_OTHER);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                if (!me) {
                    g2.setColor(NajmaTheme.BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        bubble.add(msg);
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
        List<String> msgs = Database.getChatHistory(username, target);
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
            out.println(username);
            new Thread(this::listen).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                "Connexion au serveur impossible.\n" + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void send() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        out.println(target + ":" + text);
        SwingUtilities.invokeLater(() -> addBubble("Moi", text));
        messageField.setText("");
        Database.saveMessageToDatabase(username, target, text);
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split(": ", 2);
                if (p.length == 2 && !p[0].trim().equals(username)) {
                    final String s = p[0].trim(), c = p[1].trim();
                    SwingUtilities.invokeLater(() -> addBubble(s, c));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connexion perdue.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
