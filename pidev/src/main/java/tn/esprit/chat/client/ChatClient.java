package tn.esprit.chat.client;

import tn.esprit.chat.db.LocalDatabaseManager;
import tn.esprit.chat.model.PendingMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private String username;
    private boolean isConnected = false;
    private LocalDatabaseManager dbManager;
    private Timer syncTimer;

    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnectionStatusChanged(boolean isConnected);
        void onOfflineMessageStored(String message); // Notification pour l'UI
    }

    public void connect(String serverAddress, int port, String username, MessageListener listener) {
        this.username = username;
        this.listener = listener;
        this.dbManager = LocalDatabaseManager.getInstance();

        // Tenter la connexion dans un thread séparé
        new Thread(() -> {
            try {
                clientSocket = new Socket();
                SocketAddress address = new InetSocketAddress(serverAddress, port);
                clientSocket.connect(address, 3000); // Timeout de 3 secondes

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Envoyer le nom d'utilisateur
                out.println(username);

                isConnected = true;
                listener.onConnectionStatusChanged(true);

                // Lancer la synchronisation des messages en attente
                syncPendingMessages();

                // Démarrer la vérification périodique de connexion
                startConnectionChecker(serverAddress, port);

                // Thread de réception des messages
                startMessageReceiver();

            } catch (IOException e) {
                // Mode hors ligne - pas de connexion
                isConnected = false;
                listener.onConnectionStatusChanged(false);
                listener.onMessageReceived("📴 Mode hors ligne activé - Les messages seront envoyés quand la connexion sera rétablie.");
            }
        }).start();
    }

    private void startMessageReceiver() {
        new Thread(() -> {
            try {
                String inputLine;
                while (isConnected && (inputLine = in.readLine()) != null) {
                    final String message = inputLine;
                    javafx.application.Platform.runLater(() ->
                            listener.onMessageReceived(message));
                }
            } catch (IOException e) {
                // Connexion perdue
                isConnected = false;
                javafx.application.Platform.runLater(() -> {
                    listener.onConnectionStatusChanged(false);
                    listener.onMessageReceived("❌ Connexion perdue - Passage en mode hors ligne.");
                });
            }
        }).start();
    }

    private void startConnectionChecker(String serverAddress, int port) {
        syncTimer = new Timer(true);
        syncTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected) {
                    // Tenter de se reconnecter
                    try {
                        Socket testSocket = new Socket();
                        testSocket.connect(new InetSocketAddress(serverAddress, port), 2000);
                        testSocket.close();

                        // Reconnecter le client
                        reconnect(serverAddress, port);
                    } catch (IOException e) {
                        // Toujours hors ligne
                    }
                }
            }
        }, 5000, 10000); // Vérifier toutes les 10 secondes
    }

    private void reconnect(String serverAddress, int port) {
        try {
            clientSocket = new Socket(serverAddress, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println(username);
            isConnected = true;

            javafx.application.Platform.runLater(() -> {
                listener.onConnectionStatusChanged(true);
                listener.onMessageReceived("✅ Reconnexion établie - Synchronisation des messages...");
            });

            syncPendingMessages();
            startMessageReceiver();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syncPendingMessages() {
        List<PendingMessage> pendingMessages = dbManager.getPendingMessages();

        for (PendingMessage msg : pendingMessages) {
            try {
                String recipient = msg.isGroupMessage() ? "ALL" : msg.getRecipient();
                String command = msg.isGroupMessage() ?
                        "[ALLFROM " + msg.getSenderUsername() + "]" + msg.getContent() :
                        "[SENDTO " + recipient + "]" + msg.getContent();

                out.println(command);
                out.flush();

                dbManager.markAsSynced(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!pendingMessages.isEmpty()) {
            javafx.application.Platform.runLater(() ->
                    listener.onMessageReceived("✅ " + pendingMessages.size() + " messages synchronisés."));
        }

        dbManager.cleanSyncedMessages();
    }

    public void sendMessage(String message, boolean isGroup, String recipient) {
        if (isConnected) {
            // Mode en ligne : envoyer directement
            String command = isGroup ?
                    "[ALLFROM " + username + "]" + message :
                    "[SENDTO " + recipient + "]" + message;

            out.println(command);
            out.flush();

        } else {
            // Mode hors ligne : stocker dans la file d'attente
            PendingMessage pendingMsg = new PendingMessage(
                    username,
                    isGroup ? "ALL" : recipient,
                    message,
                    isGroup
            );

            dbManager.addPendingMessage(pendingMsg);

            javafx.application.Platform.runLater(() ->
                    listener.onOfflineMessageStored("📦 Message stocké pour envoi ultérieur"));
        }
    }

    public void disconnect() {
        if (syncTimer != null) {
            syncTimer.cancel();
        }

        if (isConnected) {
            out.println("[LOGOUT]");
        }

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dbManager.close();
    }

    public boolean isConnected() {
        return isConnected;
    }
}