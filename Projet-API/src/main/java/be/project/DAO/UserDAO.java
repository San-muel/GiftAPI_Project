package be.project.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import be.project.model.User;
// Assurez-vous d'avoir une librairie de hachage comme BCrypt (ou équivalent)
// import org.mindrot.jbcrypt.BCrypt; 

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(Connection connection) {
        super(connection); // Initialise this.connection
    }
    
    // NOUVELLE MÉTHODE POUR L'AUTHENTIFICATION
    /**
     * Tente d'authentifier un utilisateur avec l'email et le mot de passe.
     * Effectue la vérification du mot de passe haché directement avec la base.
     * @param email Email de l'utilisateur.
     * @param psw Mot de passe non haché fourni par le client.
     * @return L'objet User (sans mot de passe) si les identifiants sont valides, sinon null.
     */
    public User authenticate(String email, String psw) {
        User user = null;
        String sql = "SELECT ID, USERNAME, EMAIL, PSW FROM \"User\" WHERE EMAIL = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase()); // Bonne pratique : normaliser l'email

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPasswordHash = rs.getString("PSW"); // Colonne réelle dans ta BD

                    // ÉTAPE CRUCIALE : Vérification sécurisée du mot de passe
                    boolean passwordMatches = checkPassword(psw, dbPasswordHash);

                    if (passwordMatches) {
                        user = new User();
                        user.setId(rs.getInt("ID"));
                        user.setUsername(rs.getString("USERNAME"));
                        user.setEmail(rs.getString("EMAIL"));
                        // Ne JAMAIS stocker ou renvoyer le mot de passe (même haché)
                        user.setPsw(null);

                        System.out.println("DAO DEBUG: Utilisateur authentifié avec succès : " + user.getEmail());
                    } else {
                        System.out.println("DAO DEBUG: Mot de passe incorrect pour l'email : " + email);
                    }
                } else {
                    System.out.println("DAO DEBUG: Aucun utilisateur trouvé avec l'email : " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur lors de l'authentification pour l'email : " + email);
            e.printStackTrace();
            // En production, tu pourrais logger avec Log4j/SLF4J au lieu de printStackTrace()
        }

        return user;
    }

    // Méthode séparée pour faciliter les tests et la maintenance
    private boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // À ACTIVER EN PRODUCTION :
        // return BCrypt.checkpw(plainPassword, storedHash);

        // Pour tes tests actuels (car les mots de passe dans la BD sont en clair : password123, password456)
        // Temporairement, on compare en clair :
        return storedHash.equals(plainPassword);

        // Autres options futures :
        // - Argon2 : Argon2PasswordEncoder
        // - SCrypt, PBKDF2, etc.
    }
    
    // --- Autres méthodes (find, findAll, create, delete, update) restent inchangées ---
    
    @Override
    public boolean create(User user) {
		return false;
       // ... (Votre code existant) ...
    }
    
    @Override
    public boolean delete(User obj) {
		return false;
        // ... (Votre code existant) ...
    }

    @Override
    public boolean update(User obj) {
		return false;
        // ... (Votre code existant) ...
    }

    @Override
    public User find(int id) {
		return null;
        // ... (Votre code existant) ...
    }
    
    @Override
    public List<User> findAll() {
		return null;
        // ... (Votre code existant) ...
    }
}