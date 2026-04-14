package SERVER;

import BD.Database;
import SOUNDS.WavPlayer;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<Integer, List<PrintWriter>> groupClients = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Serveur de chat en attente de connexions...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private int groupId = -1;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String initialMessage = in.readLine();

                if (initialMessage.startsWith("GROUP:")) {
                    // Connexion pour un groupe
                    String[] parts = initialMessage.split(":");
                    if (parts.length >= 3) {
                        groupId = Integer.parseInt(parts[1].trim());
                        username = parts[2].trim();
                        clients.put(username, out);

                        // Ajouter l'utilisateur au groupe
                        synchronized (groupClients) {
                            groupClients.computeIfAbsent(groupId, k -> new ArrayList<>()).add(out);
                        }

                        System.out.println(username + " a rejoint le groupe #" + groupId);
                    }
                } else {
                    // Connexion normale pour un chat privé
                    username = initialMessage;
                    synchronized (clients) {
                        clients.put(username, out);
                    }
                    System.out.println(username + " connecté.");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Message reçu: " + message);

                    if (message.startsWith("GROUP:")) {
                        broadcastToGroup(message);
                    } else {
                        String[] parts = message.split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String content = parts[1];
                            sendMessage(recipient, username + ": " + content);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnectUser();
            }
        }

        private void sendMessage(String recipient, String message) {
            synchronized (clients) {
                PrintWriter recipientOut = clients.get(recipient);
                if (recipientOut != null) {
                    recipientOut.println(message);
                    // Extraire le contenu du message
                    String[] parts = message.split(":", 2);
                    if (parts.length == 2) {
                        String sender = parts[0].trim();
                        String content = parts[1].trim();

                        // Enregistrer la notification dans la base de données
                        Database.storeNotification(sender, recipient, content);
                        try {
                            File soundFile = new File("src/SOUNDS/notif.wav");
                            WavPlayer player = new WavPlayer(soundFile);

                            if (player.open()) {
                                System.out.println("Lecture du son...");
                                player.play();
                                Thread.sleep(3000); // Attend 3 secondes pour entendre le son
                                player.stop();
                                System.out.println("Arrêt du son.");
                            } else {
                                System.out.println("Erreur lors de l'ouverture du fichier audio.");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void broadcastToGroup(String message) {
            String[] parts = message.split(":", 4);
            if (parts.length < 4) {
                return;
            }

            int targetGroupId = Integer.parseInt(parts[1].trim());
            String sender = parts[2].trim();
            String content = parts[3].trim();

            // Récupérer la liste des utilisateurs du groupe
            List<String> groupMembers = Database.getGroupMembers(targetGroupId);

            synchronized (groupClients) {
                List<PrintWriter> groupWriters = groupClients.getOrDefault(targetGroupId, new ArrayList<>());
                for (String recipient : groupMembers) {
                    if (!recipient.equals(sender)) {
                        Database.storeNotificationGroup(sender, recipient, content);
                    }
                }

                // Diffuser le message aux membres du groupe connectés
                for (PrintWriter writer : groupWriters) {
                    writer.println("GROUP:" + targetGroupId + ":" + sender + ":" + content);
                }
            }
            try {
                File soundFile = new File("src/SOUNDS/notif.wav");
                WavPlayer player = new WavPlayer(soundFile);

                if (player.open()) {
                    System.out.println("Lecture du son...");
                    player.play();
                    Thread.sleep(3000); // Attend 3 secondes pour entendre le son
                    player.stop();
                    System.out.println("Arrêt du son.");
                } else {
                    System.out.println("Erreur lors de l'ouverture du fichier audio.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void disconnectUser() {
            synchronized (clients) {
                clients.remove(username);
                Database.setUserOffline(username);
            }

            if (groupId != -1) {
                synchronized (groupClients) {
                    List<PrintWriter> groupWriters = groupClients.getOrDefault(groupId, new ArrayList<>());
                    groupWriters.remove(out);
                    if (groupWriters.isEmpty()) {
                        groupClients.remove(groupId);
                    }
                }
            }

            System.out.println(username + " déconnecté.");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
