package tn.esprit.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;

    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnectionStatusChanged(boolean isConnected);
    }

    public void connect(String serverAddress, int port, String username, MessageListener listener) throws IOException {
        this.listener = listener;
        clientSocket = new Socket(serverAddress, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.println(username);
        listener.onConnectionStatusChanged(true);

        new Thread(() -> {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    final String message = inputLine;
                    javafx.application.Platform.runLater(() -> listener.onMessageReceived(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listener != null) {
            javafx.application.Platform.runLater(() -> listener.onConnectionStatusChanged(false));
        }
    }
}