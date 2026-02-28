package tn.esprit.chat.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import tn.esprit.backend.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable, ChatClient.MessageListener {

    @FXML private TextArea messagesArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private ChatClient chatClient;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        // Demander le nom d'utilisateur (depuis la session ou via une boîte de dialogue)
        String username = Session.estConnecte() ? Session.getUtilisateur().getPrenom() : "Anonyme";
        String serverAddress = "localhost"; // À adapter selon votre configuration
        int port = 12345;

        try {
            chatClient = new ChatClient();
            chatClient.connect(serverAddress, port, username, this);
        } catch (IOException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter au serveur de chat.\n" + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && chatClient != null) {
            chatClient.sendMessage(message);
            inputField.clear();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        messagesArea.appendText(message + "\n");
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        if (!isConnected) {
            messagesArea.appendText("❌ Déconnecté du serveur.\n");
            sendButton.setDisable(true);
            inputField.setDisable(true);
        } else {
            messagesArea.appendText("✅ Connecté au serveur.\n");
            sendButton.setDisable(false);
            inputField.setDisable(false);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}