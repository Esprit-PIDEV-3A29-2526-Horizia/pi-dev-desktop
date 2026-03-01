package tn.esprit.services;

import javafx.application.Platform;
import tn.esprit.controllers.AdminController;
import tn.esprit.controllers.UserController;

public class RefreshService {
    private static AdminController adminController;
    private static UserController userController;

    public static void setAdminController(AdminController controller) {
        adminController = controller;
    }

    public static void setUserController(UserController controller) {
        userController = controller;
    }

    public static void refreshAll() {
        Platform.runLater(() -> {
            if (adminController != null) {
                adminController.loadData(); // Rafraîchit l'admin
            }
            if (userController != null) {
                userController.refreshEvents(); // Rafraîchit l'user
            }
        });
    }
}