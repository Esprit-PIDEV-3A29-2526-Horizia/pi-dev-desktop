package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.services.ResetPasswordService; // ← Vérifie cette ligne
import tn.esprit.utils.EmailService;
import tn.esprit.utils.NavigationManager;

import java.security.SecureRandom;

public class ForgotPasswordController {

    @FXML
    private VBox step1Box;
    @FXML
    private VBox step2Box;
    @FXML
    private VBox step3Box;

    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;
    @FXML
    private Label emailDisplayLabel;

    @FXML
    private Button sendResetLinkBtn;
    @FXML
    private Button verifyCodeBtn;
    @FXML
    private Button resetPasswordBtn;

    // 👉 DÉCLARATION DU SERVICE (à mettre ici)
    private ResetPasswordService resetService = new ResetPasswordService();

    private String userEmail;
    private String generatedCode;
    private SecureRandom random = new SecureRandom();

    @FXML
    public void initialize() {
        showStep(1);
        messageLabel.setVisible(false);
        System.out.println("ForgotPasswordController initialisé");
    }

    @FXML
    private void handleSendResetLink() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage("Veuillez saisir votre email", "error");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Format d'email invalide", "error");
            return;
        }

        sendResetLinkBtn.setDisable(true);
        sendResetLinkBtn.setText("Vérification...");

        new Thread(() -> {
            try {
                boolean emailExists = resetService.checkEmailExists(email);

                Thread.sleep(500);

                javafx.application.Platform.runLater(() -> {
                    if (emailExists) {
                        // Email trouvé - on envoie le code
                        showMessage("✅ Email trouvé, envoi du code...", "success");
                        sendResetLinkBtn.setText("Envoi...");

                        // Envoyer le vrai code
                        new Thread(() -> {
                            boolean sent = resetService.sendResetCode(email);
                            javafx.application.Platform.runLater(() -> {
                                if (sent) {
                                    userEmail = email;
                                    emailDisplayLabel.setText(email);
                                    showStep(2);
                                } else {
                                    showMessage("❌ Erreur d'envoi", "error");
                                    sendResetLinkBtn.setDisable(false);
                                    sendResetLinkBtn.setText("Envoyer le code");
                                }
                            });
                        }).start();

                    } else {
                        // Email non trouvé
                        showMessage("❌ Aucun compte trouvé avec cet email", "error");
                        sendResetLinkBtn.setDisable(false);
                        sendResetLinkBtn.setText("Envoyer le code");
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showMessage("Erreur : " + e.getMessage(), "error");
                    sendResetLinkBtn.setDisable(false);
                    sendResetLinkBtn.setText("Envoyer le code");
                });
                e.printStackTrace();
            }
        }).start();
    }
    /**
     * Simule l'envoi d'email pour les emails non existants (sécurité)
     */
    private void simulateEmailSending(String email) {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simuler le temps d'envoi
                javafx.application.Platform.runLater(() -> {
                    showMessage("📧 Si cet email existe, un code vous sera envoyé", "info");
                    // Ne pas passer à l'étape 2
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void handleVerifyCode() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showMessage("Veuillez saisir le code reçu", "error");
            return;
        }

        verifyCodeBtn.setDisable(true);
        verifyCodeBtn.setText("Vérification...");

        new Thread(() -> {
            try {
                // 👉 Utiliser le vrai service au lieu de la simulation
                boolean isValid = resetService.verifyCode(userEmail, code);

                javafx.application.Platform.runLater(() -> {
                    if (isValid) {
                        showStep(3);
                        showMessage("✅ Code valide. Vous pouvez changer votre mot de passe.", "success");
                    } else {
                        showMessage("❌ Code invalide ou expiré", "error");
                        codeField.clear();
                        codeField.requestFocus();
                    }
                    verifyCodeBtn.setDisable(false);
                    verifyCodeBtn.setText("Vérifier le code");
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showMessage("Erreur de vérification", "error");
                    verifyCodeBtn.setDisable(false);
                    verifyCodeBtn.setText("Vérifier le code");
                });
            }
        }).start();
    }
    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Les mots de passe ne correspondent pas", "error");
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
            return;
        }

        resetPasswordBtn.setDisable(true);
        resetPasswordBtn.setText("Réinitialisation...");

        String code = codeField.getText().trim();

        new Thread(() -> {
            try {
                // 👉 Utiliser le vrai service
                boolean reset = resetService.resetPassword(userEmail, newPassword, code);

                javafx.application.Platform.runLater(() -> {
                    if (reset) {
                        showAlert("Succès", "✅ Votre mot de passe a été réinitialisé !");
                        NavigationManager.loadView("/fxml/Login.fxml");
                    } else {
                        showMessage("❌ Échec de la réinitialisation. Code invalide ou expiré.", "error");
                        // Retour à l'étape 2
                        showStep(2);
                    }
                    resetPasswordBtn.setDisable(false);
                    resetPasswordBtn.setText("Réinitialiser");
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showMessage("Erreur : " + e.getMessage(), "error");
                    resetPasswordBtn.setDisable(false);
                    resetPasswordBtn.setText("Réinitialiser");
                });
            }
        }).start();
    }


    /**
     * Retour à la page de connexion
     */
    @FXML
    private void handleBackToLogin() {
        NavigationManager.loadView("/fxml/Login.fxml");
    }

    @FXML
    private void handleResendCode() {
        sendResetLinkBtn.setDisable(true);
        sendResetLinkBtn.setText("Renvoi...");

        new Thread(() -> {
            boolean sent = resetService.sendResetCode(userEmail);

            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    showMessage("📧 Nouveau code envoyé !", "success");
                    codeField.clear();
                } else {
                    showMessage("❌ Erreur lors du renvoi", "error");
                }
                sendResetLinkBtn.setDisable(false);
                sendResetLinkBtn.setText("Renvoyer le code");
            });
        }).start();
    }
    /**
     * Affiche une étape
     */
    private void showStep(int step) {
        step1Box.setVisible(step == 1);
        step1Box.setManaged(step == 1);
        step2Box.setVisible(step == 2);
        step2Box.setManaged(step == 2);
        step3Box.setVisible(step == 3);
        step3Box.setManaged(step == 3);
    }

    /**
     * Affiche un message temporaire
     */
    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #81AE8D; -fx-font-weight: bold;");
        }
        messageLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
            javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
        }).start();
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void testEmailConfiguration() {
        new Thread(() -> {
            boolean result = EmailService.testConfiguration();
            javafx.application.Platform.runLater(() -> {
                if (result) {
                    showAlert("Succès", "✅ Configuration email OK ! Vérifiez votre boîte mail.");
                } else {
                    showAlert("Erreur", "❌ Échec de la configuration. Vérifiez vos identifiants.");
                }
            });
        }).start();
    }
}