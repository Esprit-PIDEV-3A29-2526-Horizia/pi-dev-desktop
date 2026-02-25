package tn.esprit.services;

import tn.esprit.utils.EmailService;
import tn.esprit.utils.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;

public class ResetPasswordService {

    private static final SecureRandom random = new SecureRandom();
    private Connection connection;

    public ResetPasswordService() {
        // Initialiser la connexion à la base de données
        this.connection = MyDataBase.getInstance().getMyConnection();
    }

    /**
     * Génère un code aléatoire à 6 chiffres
     */
    public String generateResetCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Vérifie si l'email existe dans la base
     */
    private boolean userExists(String email) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Envoie un code de réinitialisation par email
     */
    public boolean sendResetCode(String userEmail) {
        try {
            // Vérifier si l'email existe
            if (!userExists(userEmail)) {
                System.out.println("ℹ️ Email non trouvé (sécurité) : " + userEmail);
                return true; // Retourner true pour ne pas révéler l'existence
            }

            // Générer le code
            String code = generateResetCode();

            // Sauvegarder en base avec expiration (15 minutes)
            saveResetCode(userEmail, code);

            // Envoyer l'email
            boolean emailSent = EmailService.sendResetPasswordEmail(userEmail, code);

            if (emailSent) {
                System.out.println("✅ Code envoyé à : " + userEmail);
                return true;
            } else {
                System.out.println("❌ Échec d'envoi d'email");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sauvegarde le code en base de données
     */
    private void saveResetCode(String email, String code) throws SQLException {
        // Supprimer les anciens codes
        String deleteSql = "DELETE FROM password_resets WHERE email = ?";
        try (PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            deletePs.setString(1, email);
            deletePs.executeUpdate();
        }

        // Insérer le nouveau code
        String insertSql = "INSERT INTO password_resets (email, code, expires_at) VALUES (?, ?, ?)";
        try (PreparedStatement insertPs = connection.prepareStatement(insertSql)) {
            insertPs.setString(1, email);
            insertPs.setString(2, code);
            insertPs.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            insertPs.executeUpdate();

            System.out.println("💾 Code sauvegardé en base pour : " + email);
        }
    }

    /**
     * Vérifie si le code est valide
     */
    public boolean verifyCode(String email, String code) throws SQLException {
        String sql = "SELECT * FROM password_resets WHERE email = ? AND code = ? AND expires_at > ? AND used = FALSE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, code);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                boolean isValid = rs.next();
                if (isValid) {
                    System.out.println("✅ Code valide pour : " + email);
                } else {
                    System.out.println("❌ Code invalide ou expiré pour : " + email);
                }
                return isValid;
            }
        }
    }

    /**
     * Réinitialise le mot de passe
     */
    public boolean resetPassword(String email, String newPassword, String code) {
        try {
            // Vérifier le code d'abord
            if (!verifyCode(email, code)) {
                return false;
            }

            // Mettre à jour le mot de passe
            String sql = "UPDATE user SET password = ? WHERE email = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                // Hacher le mot de passe avec BCrypt
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));

                ps.setString(1, hashedPassword);
                ps.setString(2, email);

                int updated = ps.executeUpdate();

                if (updated > 0) {
                    // Marquer le code comme utilisé
                    markCodeAsUsed(email, code);
                    System.out.println("✅ Mot de passe réinitialisé pour : " + email);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marque le code comme utilisé
     */
    private void markCodeAsUsed(String email, String code) throws SQLException {
        String sql = "UPDATE password_resets SET used = TRUE WHERE email = ? AND code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, code);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime les codes expirés (à appeler périodiquement)
     */
    public void cleanupExpiredCodes() throws SQLException {
        String sql = "DELETE FROM password_resets WHERE expires_at < ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("🧹 " + deleted + " codes expirés supprimés");
            }
        }
    }



    /**
     * Vérifie si un email existe dans la table user
     */
    public boolean checkEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'email: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}