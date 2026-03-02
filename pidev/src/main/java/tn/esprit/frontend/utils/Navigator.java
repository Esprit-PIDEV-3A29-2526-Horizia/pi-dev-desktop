package tn.esprit.frontend.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Navigator {
    private static StackPane contentPane;
    private static Stage primaryStage;

    public static void setContentPane(StackPane pane) {
        contentPane = pane;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadView(String fxmlPath) {
        try {
            if (contentPane != null) {
                Node view = FXMLLoader.load(Navigator.class.getResource(fxmlPath));
                contentPane.getChildren().clear();
                contentPane.getChildren().add(view);
            } else if (primaryStage != null) {
                Parent root = FXMLLoader.load(Navigator.class.getResource(fxmlPath));
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadInSamePane(String fxmlPath, Node sourceNode) {
        try {
            Node view = FXMLLoader.load(Navigator.class.getResource(fxmlPath));
            StackPane pane = (StackPane) sourceNode.getScene().lookup("#contentPane");
            if (pane != null) {
                pane.getChildren().clear();
                pane.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}