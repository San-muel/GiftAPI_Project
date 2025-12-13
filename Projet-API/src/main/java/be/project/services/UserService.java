package be.project.services;

import be.project.DAO.UserDAO;
import be.project.model.User;
// Importation de votre Singleton
import be.project.singleton.SingletonConnection; 

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {
    
    // Remplacement du placeholder par l'appel à votre Singleton
    private Connection getConnection() {
        // Le Singleton gère les exceptions SQL au démarrage (bloc static)
        // et retourne la connexion unique.
        return SingletonConnection.getConnection(); 
    }
    
    /**
     * Tente d'authentifier un utilisateur en appelant le DAO pour la vérification DB.
     * ...
     */
    public User authenticate(String email, String psw) {
        
        System.out.println("SERVICE DEBUG: Tentative d'authentification pour " + email);
        
        // Nous n'avons pas besoin d'ouvrir/fermer la connexion ici
        // car le Singleton la gère.
        Connection conn = null; 
        User authenticatedUser = null;
        
        try {
            // 1. OBTENTION DE LA CONNEXION (depuis le Singleton)
            conn = getConnection(); 
            
            // VERIFICATION CRUCIALE: Si le bloc static a échoué, conn sera null
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return null;
            }

            // 2. INSTANCIATION DU DAO : Injection de la connexion Singleton.
            UserDAO userDAO = new UserDAO(conn); 
            
            // 3. APPEL AU DAO : Exécution de la vérification des identifiants en DB.
            authenticatedUser = userDAO.authenticate(email, psw); 
            
            if (authenticatedUser != null) {
                // Logique de sécurité : Génération du Token JWT.
                String token = generateJwtToken(authenticatedUser);
                authenticatedUser.setToken(token);
                System.out.println("SERVICE DEBUG: Authentification réussie. Token stocké dans le modèle.");
            } else {
                System.out.println("SERVICE DEBUG: Échec d'authentification : Identifiants invalides.");
            }

        } catch (SQLException e) {
            // Se produit si conn.isClosed() échoue ou si le DAO rencontre une erreur SQL
            System.err.println("SERVICE ERROR: Erreur SQL lors de l'authentification.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service.");
            e.printStackTrace();
        } finally {
            // La connexion du Singleton est généralement laissée ouverte.
            // Si vous utilisiez un pool, vous feriez : conn.close(); pour la rendre au pool.
            // Ici, on ne fait rien pour laisser le singleton ouvert.
        }
        
        return authenticatedUser; 
    }

    /**
     * Logique de Sécurité : Génère un Token JWT unique.
     * ...
     */
    public String generateJwtToken(User user) {
        String payload = user.getId() + ":" + user.getEmail() + ":" + System.currentTimeMillis();
        return "fake-jwt-token-" + payload;
    }
}