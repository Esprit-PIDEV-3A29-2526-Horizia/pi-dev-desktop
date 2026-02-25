package tn.esprit.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import java.util.regex.Pattern;

public class ValidationUtil {

    /**
     * Validation email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email.trim()).matches();
    }

    /**
     * Validation téléphone (8 chiffres)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String phoneRegex = "^[0-9]{8}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone.trim()).matches();
    }

    /**
     * Validation téléphone international (8-15 chiffres, + optionnel)
     */
    public static boolean isValidPhoneInternational(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String phoneRegex = "^[+]?[0-9]{8,15}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone.trim()).matches();
    }

    /**
     * Validation nom/prénom (2-50 caractères, lettres, espaces et tirets)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String nameRegex = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s-]{2,50}$";
        Pattern pattern = Pattern.compile(nameRegex);
        return pattern.matcher(name.trim()).matches();
    }

    /**
     * Validation mot de passe (minimum 6 caractères)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validation mot de passe fort (minimum 8 caractères, majuscule, minuscule, chiffre, spécial)
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUpper && hasLower && hasDigit;
        // hasSpecial est optionnel, décommentez si vous voulez l'exiger
        // return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Validation champ non vide
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validation adresse (optionnelle, minimum 5 caractères si renseignée)
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) return true; // Optionnel
        return address.trim().length() >= 5;
    }

    /**
     * Validation âge (entre 18 et 120 ans)
     */
    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 120;
    }

    /**
     * Validation code postal (5 chiffres)
     */
    public static boolean isValidPostalCode(String code) {
        if (code == null || code.trim().isEmpty()) return false;
        String codeRegex = "^[0-9]{5}$";
        Pattern pattern = Pattern.compile(codeRegex);
        return pattern.matcher(code.trim()).matches();
    }

    /**
     * Validation URL
     */
    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        Pattern pattern = Pattern.compile(urlRegex);
        return pattern.matcher(url.trim()).matches();
    }

    /**
     * Mettre en surbrillance champ texte
     */
    public static void setFieldStyle(TextField field, boolean isValid) {
        if (field == null) return;
        if (isValid) {
            field.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5;");
        } else {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #fff5f5;");
        }
    }

    /**
     * Mettre en surbrillance champ mot de passe
     */
    public static void setFieldStyle(PasswordField field, boolean isValid) {
        if (field == null) return;
        if (isValid) {
            field.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5;");
        } else {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #fff5f5;");
        }
    }

    /**
     * Mettre en surbrillance ComboBox
     */
    public static void setComboBoxStyle(ComboBox<?> comboBox, boolean isValid) {
        if (comboBox == null) return;
        if (isValid) {
            comboBox.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            comboBox.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 5;");
        }
    }

    /**
     * Nettoyer une chaîne (supprimer les espaces multiples)
     */
    public static String cleanString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Capitaliser la première lettre
     */
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Vérifier si deux mots de passe correspondent
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    /**
     * Obtenir la force du mot de passe (0-4)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int strength = 0;
        if (password.length() >= 6) strength++;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        return Math.min(strength, 4); // Max 4
    }

    /**
     * Message selon la force du mot de passe
     */
    public static String getPasswordStrengthMessage(String password) {
        int strength = getPasswordStrength(password);
        switch (strength) {
            case 0: return "❌ Trop faible";
            case 1: return "⚠️ Faible";
            case 2: return "🟡 Moyen";
            case 3: return "🟢 Fort";
            case 4: return "✅ Très fort";
            default: return "";
        }
    }
}