package tn.esprit.chat.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.backend.utils.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable, ChatClient.MessageListener {

    @FXML private TextArea messagesArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Label connectionStatusLabel;
    @FXML private ToggleGroup messageType;
    @FXML private RadioButton groupChatRadio;
    @FXML private RadioButton privateChatRadio;
    @FXML private ComboBox<String> userCombo;

    private ChatClient chatClient;
    private String username;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        // Le groupe est sélectionné par défaut
        groupChatRadio.setSelected(true);
        // Permettre de cliquer sur "Privé" (supprimer setDisable)
        privateChatRadio.setDisable(false);

        // Gérer l'activation de la combo en fonction du mode sélectionné
        groupChatRadio.selectedProperty().addListener((obs, oldVal, isGroup) -> {
            // Si on est en mode groupe, désactiver la combo ; en mode privé, l'activer
            userCombo.setDisable(isGroup);
        });
        // Initialement, on est en groupe, donc désactiver la combo
        userCombo.setDisable(true);

        // Pour tester, on remplit la combo avec des noms factices
        // (plus tard, tu pourras récupérer la vraie liste des utilisateurs connectés)
        userCombo.getItems().addAll("Alice", "Bob", "Charlie");

        username = Session.estConnecte() ?
                Session.getUtilisateur().getPrenom() + " " + Session.getUtilisateur().getNom() :
                "Anonyme";

        String serverAddress = "localhost";
        int port = 12345;

        chatClient = new ChatClient();
        chatClient.connect(serverAddress, port, username, this);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && chatClient != null) {
            boolean isGroup = groupChatRadio.isSelected();
            String recipient = isGroup ? "ALL" : userCombo.getValue();
            // Pour l'instant, même en privé, le message est diffusé à tous
            // (amélioration possible côté serveur)
            chatClient.sendMessage(message, isGroup, recipient);
            inputField.clear();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        messagesArea.appendText(message + "\n");
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        if (isConnected) {
            connectionStatusLabel.setText("🟢 Connecté");
            connectionStatusLabel.setStyle("-fx-text-fill: green;");
            sendButton.setDisable(false);
            inputField.setDisable(false);
        } else {
            connectionStatusLabel.setText("🔴 Hors ligne");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            sendButton.setDisable(false);
            inputField.setDisable(false);
        }
    }

    @Override
    public void onOfflineMessageStored(String message) {
        messagesArea.appendText("📦 " + message + "\n");
    }

    public void shutdown() {
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }
}