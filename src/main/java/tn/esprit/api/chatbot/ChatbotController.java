package tn.esprit.api.chatbot;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class ChatbotController {
    private VBox chatPanel;
    private VBox chatBox;
    private TextField questionField;
    private Button chatButton;
    private boolean chatVisible = false;
    private GeminiService geminiService;

    public ChatbotController() {
        this.geminiService = new GeminiService();
    }

    public StackPane createChatbot() {
        StackPane container = new StackPane();
        container.setAlignment(Pos.BOTTOM_RIGHT);
        container.setPadding(new Insets(20));
        container.setPickOnBounds(false);

        // Bouton flottant avec animation
        chatButton = new Button("💬");
        chatButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 28px;" +
                        "-fx-min-width: 65px;" +
                        "-fx-min-height: 65px;" +
                        "-fx-max-width: 65px;" +
                        "-fx-max-height: 65px;" +
                        "-fx-background-radius: 32.5px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0.5, 0, 5);" +
                        "-fx-border-color: rgba(255,255,255,0.3);" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 32.5px;"
        );

        // Animation du bouton
        chatButton.setOnMouseEntered(e -> {
            chatButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #764ba2, #667eea);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 28px;" +
                            "-fx-min-width: 70px;" +
                            "-fx-min-height: 70px;" +
                            "-fx-max-width: 70px;" +
                            "-fx-max-height: 70px;" +
                            "-fx-background-radius: 35px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0.5, 0, 5);" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 35px;"
            );
        });

        chatButton.setOnMouseExited(e -> {
            chatButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 28px;" +
                            "-fx-min-width: 65px;" +
                            "-fx-min-height: 65px;" +
                            "-fx-max-width: 65px;" +
                            "-fx-max-height: 65px;" +
                            "-fx-background-radius: 32.5px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0.5, 0, 5);" +
                            "-fx-border-color: rgba(255,255,255,0.3);" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 32.5px;"
            );
        });

        // Panneau de chat amélioré
        chatPanel = createChatPanel();
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);

        chatButton.setOnAction(e -> toggleChat());

        container.getChildren().addAll(chatPanel, chatButton);
        return container;
    }

    private VBox createChatPanel() {
        VBox panel = new VBox(0);
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0.2, 0, 10);" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 20px;"
        );
        panel.setPrefWidth(380);
        panel.setPrefHeight(550);
        panel.setMaxWidth(380);
        panel.setMaxHeight(550);

        // En-tête avec gradient
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-background-radius: 20px 20px 0 0;" +
                        "-fx-padding: 15px 20px;"
        );

        // Avatar du chatbot
        Label avatar = new Label("🤖");
        avatar.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-text-fill: white;"
        );

        VBox titleBox = new VBox(3);
        titleBox.setPadding(new Insets(0, 0, 0, 10));

        Label title = new Label("Assistant Voyages");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label status = new Label("● en ligne");
        status.setStyle("-fx-font-size: 11px; -fx-text-fill: #a5d6ff;");

        titleBox.getChildren().addAll(title, status);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 5px;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 5px; -fx-background-radius: 15px;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 5px;"));
        closeBtn.setOnAction(e -> toggleChat());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, titleBox, spacer, closeBtn);

        // Zone des messages avec fond léger
        StackPane messageContainer = new StackPane();
        messageContainer.setStyle("-fx-background-color: #f8f9ff;");
        messageContainer.setPadding(new Insets(15));

        chatBox = new VBox(12);
        chatBox.setStyle("-fx-background-color: transparent;");

        // Message de bienvenue
        addBotMessage("🌟 **Bienvenue sur votre assistant voyages !** 🌟\n\nJe suis là pour vous aider à trouver le voyage de vos rêves. N'hésitez pas à me poser des questions sur nos destinations, prix et disponibilités !");

        // AJOUT DES SUGGESTIONS ICI
        addSuggestions();

        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setPrefHeight(370);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        messageContainer.getChildren().add(scrollPane);

        // Zone de saisie avec style moderne
        HBox inputBox = new HBox(10);
        inputBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 0 0 20px 20px;" +
                        "-fx-padding: 15px 15px 20px 15px;" +
                        "-fx-border-color: #e0e0ff;" +
                        "-fx-border-width: 1px 0 0 0;"
        );
        inputBox.setAlignment(Pos.CENTER);

        questionField = new TextField();
        questionField.setPromptText("Écrivez votre message...");
        questionField.setStyle(
                "-fx-background-color: #f0f2ff;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 10px 15px;" +
                        "-fx-font-size: 13px;" +
                        "-fx-prompt-text-fill: #a0a0c0;"
        );
        questionField.setPrefHeight(40);
        HBox.setHgrow(questionField, Priority.ALWAYS);

        Button sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-max-width: 40px;" +
                        "-fx-max-height: 40px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 10, 0.3, 0, 3);"
        );

        sendBtn.setOnMouseEntered(e ->
                sendBtn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #764ba2, #667eea);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 42px;" +
                                "-fx-min-height: 42px;" +
                                "-fx-max-width: 42px;" +
                                "-fx-max-height: 42px;" +
                                "-fx-background-radius: 21px;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.7), 15, 0.3, 0, 5);"
                )
        );

        sendBtn.setOnMouseExited(e ->
                sendBtn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 10, 0.3, 0, 3);"
                )
        );

        sendBtn.setOnAction(e -> sendQuestion());
        questionField.setOnAction(e -> sendQuestion());

        inputBox.getChildren().addAll(questionField, sendBtn);

        panel.getChildren().addAll(header, messageContainer, inputBox);
        return panel;
    }

    /**
     * Ajoute des boutons de suggestions de questions
     */
    private void addSuggestions() {
        HBox suggestionsBox = new HBox(8);
        suggestionsBox.setAlignment(Pos.CENTER);
        suggestionsBox.setPadding(new Insets(5, 0, 10, 0));

        String[] suggestions = {"🌍 Destinations", "💰 Prix", "📅 Dates", "🎫 Disponibilités"};
        String[] questions = {"Quelles sont les destinations ?", "Quels sont les prix ?", "Quand partent les voyages ?", "Y a-t-il des places ?"};

        for (int i = 0; i < suggestions.length; i++) {
            Button suggestionBtn = new Button(suggestions[i]);
            int index = i;
            suggestionBtn.setStyle(
                    "-fx-background-color: #f0f2ff;" +
                            "-fx-text-fill: #4a5568;" +
                            "-fx-font-size: 11px;" +
                            "-fx-background-radius: 15px;" +
                            "-fx-padding: 6px 12px;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: #e0e0ff;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-border-width: 1px;"
            );

            suggestionBtn.setOnMouseEntered(e ->
                    suggestionBtn.setStyle(
                            "-fx-background-color: #667eea;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 11px;" +
                                    "-fx-background-radius: 15px;" +
                                    "-fx-padding: 6px 12px;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-border-color: #667eea;" +
                                    "-fx-border-radius: 15px;"
                    )
            );

            suggestionBtn.setOnMouseExited(e ->
                    suggestionBtn.setStyle(
                            "-fx-background-color: #f0f2ff;" +
                                    "-fx-text-fill: #4a5568;" +
                                    "-fx-font-size: 11px;" +
                                    "-fx-background-radius: 15px;" +
                                    "-fx-padding: 6px 12px;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-border-color: #e0e0ff;" +
                                    "-fx-border-radius: 15px;" +
                                    "-fx-border-width: 1px;"
                    )
            );

            suggestionBtn.setOnAction(e -> {
                questionField.setText(questions[index]);
                sendQuestion();
            });

            suggestionsBox.getChildren().add(suggestionBtn);
        }

        chatBox.getChildren().add(suggestionsBox);
    }

    private void toggleChat() {
        chatVisible = !chatVisible;

        if (chatVisible) {
            chatPanel.setVisible(true);
            chatPanel.setManaged(true);
            chatButton.setText("✕");
            chatButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #e74c3c, #c0392b);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 24px;" +
                            "-fx-min-width: 65px;" +
                            "-fx-min-height: 65px;" +
                            "-fx-max-width: 65px;" +
                            "-fx-max-height: 65px;" +
                            "-fx-background-radius: 32.5px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.5), 15, 0.5, 0, 5);" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 32.5px;"
            );

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), chatPanel);
            tt.setFromX(400);
            tt.setToX(0);
            tt.play();
        } else {
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), chatPanel);
            tt.setFromX(0);
            tt.setToX(400);
            tt.setOnFinished(e -> {
                chatPanel.setVisible(false);
                chatPanel.setManaged(false);
                chatButton.setText("💬");
                chatButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 28px;" +
                                "-fx-min-width: 65px;" +
                                "-fx-min-height: 65px;" +
                                "-fx-max-width: 65px;" +
                                "-fx-max-height: 65px;" +
                                "-fx-background-radius: 32.5px;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0.5, 0, 5);" +
                                "-fx-border-color: rgba(255,255,255,0.3);" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 32.5px;"
                );
            });
            tt.play();
        }
    }

    private void sendQuestion() {
        String question = questionField.getText().trim();
        if (question.isEmpty()) return;

        addUserMessage(question);
        questionField.clear();

        questionField.setDisable(true);

        // Indicateur de chargement amélioré
        HBox loadingBox = new HBox(5);
        loadingBox.setAlignment(Pos.CENTER_LEFT);

        Label loadingLabel = new Label("🤔");
        loadingLabel.setStyle("-fx-font-size: 20px;");

        Label loadingText = new Label("L'assistant réfléchit...");
        loadingText.setStyle("-fx-text-fill: #667eea; -fx-font-style: italic; -fx-font-size: 12px;");

        loadingBox.getChildren().addAll(loadingLabel, loadingText);
        loadingBox.setId("loading-message");
        chatBox.getChildren().add(loadingBox);

        geminiService.askQuestion(question, new ChatbotCallback() {
            @Override
            public void onSuccess(String response) {
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().removeIf(node ->
                            "loading-message".equals(node.getId()));
                    addBotMessage(response);
                    questionField.setDisable(false);
                    questionField.requestFocus();
                });
            }

            @Override
            public void onError(String error) {
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().removeIf(node ->
                            "loading-message".equals(node.getId()));
                    addBotMessage("❌ " + error);
                    questionField.setDisable(false);
                });
            }
        });
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(0, 0, 0, 50));

        VBox messageContent = new VBox();
        messageContent.setAlignment(Pos.CENTER_RIGHT);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
                "-fx-background-color: linear-gradient(to bottom left, #667eea, #764ba2);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10px 15px;" +
                        "-fx-background-radius: 18px 5px 18px 18px;" +
                        "-fx-font-size: 13px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 8, 0.2, 0, 2);"
        );
        messageLabel.setMaxWidth(250);

        Label timeLabel = new Label("maintenant");
        timeLabel.setStyle("-fx-text-fill: #a0a0c0; -fx-font-size: 9px; -fx-padding: 5px 5px 0 0;");

        messageContent.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(messageContent);
        chatBox.getChildren().add(messageBox);
        chatBox.layout();
    }


    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(0, 50, 0, 0));
        messageBox.setStyle("-fx-animation: fadeIn 0.3s;");

        // Avatar du bot
        Label botAvatar = new Label("🤖");
        botAvatar.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-padding: 0 10px 0 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 5, 0.3, 0, 2);"
        );

        VBox messageContent = new VBox(10);
        messageContent.setAlignment(Pos.CENTER_LEFT);
        messageContent.setMaxWidth(280);

        // Vérifier si c'est une liste de destinations
        if (message.contains("* **")) {
            // === Message d'accueil personnalisé ===
            Label welcomeLabel = new Label("✨ Bonjour ! Voici nos destinations ✨");
            welcomeLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #667eea;" +
                            "-fx-padding: 5px 0 10px 0;" +
                            "-fx-alignment: center;"
            );
            welcomeLabel.setMaxWidth(Double.MAX_VALUE);
            welcomeLabel.setAlignment(Pos.CENTER);
            messageContent.getChildren().add(welcomeLabel);

            // === Créer les cartes de destinations ===
            String[] lines = message.split("\n");
            int countDestinations = 0;

            for (String line : lines) {
                if (line.contains("* **")) {
                    countDestinations++;
                    // Extraire le nom de la destination
                    String dest = line.replace("* **", "").replace("**", "").trim();

                    // Créer une carte pour la destination
                    HBox card = createDestinationCard(dest);
                    messageContent.getChildren().add(card);
                }
            }

            // === Message de fin avec statistiques ===
            VBox footerBox = new VBox(5);
            footerBox.setPadding(new Insets(10, 0, 0, 0));

            Label instructionLabel = new Label("👇 Cliquez sur une destination pour plus de détails !");
            instructionLabel.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: #667eea;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-style: italic;"
            );

            Label statsLabel = new Label("📊 " + countDestinations + " destinations disponibles");
            statsLabel.setStyle(
                    "-fx-font-size: 10px;" +
                            "-fx-text-fill: #95a5a6;"
            );

            footerBox.getChildren().addAll(instructionLabel, statsLabel);
            messageContent.getChildren().add(footerBox);

        } else {
            // === Message normal (pas une liste) ===
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #2d3748;" +
                            "-fx-padding: 12px 15px;" +
                            "-fx-background-radius: 5px 18px 18px 18px;" +
                            "-fx-font-size: 13px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.1, 0, 2);" +
                            "-fx-border-color: #e0e0ff;" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 5px 18px 18px 18px;"
            );
            messageLabel.setMaxWidth(250);
            messageContent.getChildren().add(messageLabel);
        }

        // === Horodatage ===
        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setStyle(
                "-fx-text-fill: #a0a0c0;" +
                        "-fx-font-size: 9px;" +
                        "-fx-padding: 5px 0 0 5px;"
        );
        messageContent.getChildren().add(timeLabel);

        // Assemblage du message
        HBox botBox = new HBox(5);
        botBox.setAlignment(Pos.CENTER_LEFT);
        botBox.getChildren().addAll(botAvatar, messageContent);

        messageBox.getChildren().add(botBox);
        chatBox.getChildren().add(messageBox);

        // Animation d'apparition
        messageBox.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                Duration.millis(300), messageBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        chatBox.layout();
    }


    private HBox createDestinationCard(String destination) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: linear-gradient(to right, #f8f9ff, #ffffff);" +
                        "-fx-background-radius: 15px;" +
                        "-fx-padding: 10px 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.15), 8, 0.2, 0, 3);" +
                        "-fx-border-color: #e0e0ff;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;" +
                        "-fx-transition: all 0.2s;"
        );
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                            "-fx-background-radius: 15px;" +
                            "-fx-padding: 10px 15px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0.3, 0, 5);" +
                            "-fx-border-color: transparent;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-cursor: hand;"
            );
            for (javafx.scene.Node node : card.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                } else if (node instanceof VBox) {
                    for (javafx.scene.Node subNode : ((VBox) node).getChildren()) {
                        if (subNode instanceof Label) {
                            ((Label) subNode).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to right, #f8f9ff, #ffffff);" +
                            "-fx-background-radius: 15px;" +
                            "-fx-padding: 10px 15px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.15), 8, 0.2, 0, 3);" +
                            "-fx-border-color: #e0e0ff;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-border-width: 1px;" +
                            "-fx-cursor: hand;"
            );
            resetCardColors(card, destination);
        });
        card.setOnMouseClicked(e -> {
            questionField.setText("Parlez-moi de " + destination);
            sendQuestion();
        });
        Label emojiLabel = new Label(getEmojiForDestination(destination));
        emojiLabel.setStyle("-fx-font-size: 28px; -fx-min-width: 45px; -fx-alignment: center;");
        VBox textBox = new VBox(4);
        Label destLabel = new Label(destination);
        destLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2d3748;"
        );
        Label disponibilite = new Label("✓ disponible");
        disponibilite.setStyle(
                "-fx-background-color: #e1f7e1;" +
                        "-fx-text-fill: #27ae60;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 3px 10px;" +
                        "-fx-border-color: #a3e4a3;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;"
        );

        textBox.getChildren().addAll(destLabel, disponibilite);
        card.getChildren().addAll(emojiLabel, textBox);

        return card;
    }


    private void resetCardColors(HBox card, String destination) {
        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #2d3748;");
            } else if (node instanceof VBox) {
                for (javafx.scene.Node subNode : ((VBox) node).getChildren()) {
                    if (subNode instanceof Label) {
                        if (((Label) subNode).getText().equals(destination)) {
                            ((Label) subNode).setStyle(
                                    "-fx-font-size: 15px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-text-fill: #2d3748;"
                            );
                        } else {
                            ((Label) subNode).setStyle(
                                    "-fx-background-color: #e1f7e1;" +
                                            "-fx-text-fill: #27ae60;" +
                                            "-fx-font-size: 10px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-radius: 12px;" +
                                            "-fx-padding: 3px 10px;" +
                                            "-fx-border-color: #a3e4a3;" +
                                            "-fx-border-radius: 12px;" +
                                            "-fx-border-width: 1px;"
                            );
                        }
                    }
                }
            }
        }
    }


    private String getEmojiForDestination(String dest) {
        dest = dest.toLowerCase();
        if (dest.contains("bali")) return "🌴";
        if (dest.contains("sydney") || dest.contains("australie")) return "🏖️";
        if (dest.contains("barcelone") || dest.contains("espagne")) return "🇪🇸";
        if (dest.contains("tokyo") || dest.contains("japon")) return "🏯";
        if (dest.contains("suisse")) return "🇨🇭";
        if (dest.contains("tozeur") || dest.contains("tunisie")) return "🏜️";
        if (dest.contains("rome") || dest.contains("italie")) return "🇮🇹";
        if (dest.contains("grèce") || dest.contains("athènes")) return "🇬🇷";
        if (dest.contains("paris") || dest.contains("france")) return "🇫🇷";
        if (dest.contains("londres") || dest.contains("royaume-uni")) return "🇬🇧";
        if (dest.contains("new york") || dest.contains("usa")) return "🗽";
        if (dest.contains("dubai")) return "🌆";
        if (dest.contains("maroc")) return "🇲🇦";
        if (dest.contains("egypte")) return "🇪🇬";
        return "🌍";
    }


    private String getCurrentTime() {
        java.time.LocalTime time = java.time.LocalTime.now();
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}