package tn.esprit.controllers;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import java.util.regex.Pattern;

public class ValidationUtil {

    // Validation email
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // Validation téléphone (8 chiffres)
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        String phoneRegex = "^[0-9]{8}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }

    // Validation téléphone international (optionnel)
    public static boolean isValidPhoneInternational(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        String phoneRegex = "^[+]?[0-9]{8,15}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }

    // Validation nom/prénom (2-50 caractères, lettres et espaces)
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) return false;
        String nameRegex = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s-]{2,50}$";
        Pattern pattern = Pattern.compile(nameRegex);
        return pattern.matcher(name.trim()).matches();
    }

    // Validation mot de passe (min 6 caractères)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validation mot de passe fort (optionnel)
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Validation champ non vide
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Validation adresse (optionnelle)
    public static boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) return true; // Optionnel
        return address.trim().length() >= 5;
    }

    // Mettre en surbrillance champ texte
    public static void setFieldStyle(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5;");
        }
    }

    // Mettre en surbrillance champ mot de passe
    public static void setFieldStyle(PasswordField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5;");
        }
    }

    // Mettre en surbrillance ComboBox
    public static void setComboBoxStyle(ComboBox<?> comboBox, boolean isValid) {
        if (isValid) {
            comboBox.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            comboBox.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5;");
        }
    }

    // Nettoyer une chaîne (supprimer les espaces multiples)
    public static String cleanString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }
}