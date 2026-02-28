package tn.esprit.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            username = in.readLine();
            System.out.println("👤 " + username + " a rejoint le chat.");
            server.broadcastMessage("👤 " + username + " a rejoint la discussion.", this);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[" + username + "] : " + inputLine);
                server.broadcastMessage("[" + username + "] : " + inputLine, this);
            }

        } catch (IOException e) {
            System.out.println("❌ Connexion perdue avec " + username);
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
            server.broadcastMessage("👋 " + username + " a quitté le chat.", this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}