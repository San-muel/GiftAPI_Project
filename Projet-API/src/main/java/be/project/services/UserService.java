package be.project.services;

import be.project.DAO.UserDAO;
import be.project.model.User;
import be.project.singleton.SingletonConnection; 

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {
    
    // Remplacement du placeholder par l'appel à votre Singleton
    private Connection getConnection() {
        return SingletonConnection.getConnection(); 
    }
    
    /**
     * Tente d'authentifier un utilisateur en appelant le DAO pour la vérification DB.
     * Le DAO retourne un objet User entièrement hydraté.
     *
     * @param email Email de l'utilisateur.
     * @param psw Mot de passe en clair.
     * @return L'objet User complet avec token et relations, ou null.
     */
    public User authenticate(String email, String psw) {
        
        System.out.println("SERVICE DEBUG: Tentative d'authentification pour " + email);
        
        Connection conn = null; 
        User authenticatedUser = null;
        
        try {
            // 1. OBTENTION DE LA CONNEXION
            conn = getConnection(); 
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return null;
            }

            // 2. INSTANCIATION DU DAO
            UserDAO userDAO = new UserDAO(conn); 
            
            // 3. APPEL AU DAO : L'objet retourné est MAINTENANT HYDRATÉ COMPLÈTEMENT.
            authenticatedUser = userDAO.authenticate(email, psw); 
            
            if (authenticatedUser != null) {
                // Logique de sécurité : Génération du Token JWT.
                String token = generateJwtToken(authenticatedUser);
                authenticatedUser.setToken(token);
                System.out.println("SERVICE DEBUG: Authentification et hydratation réussies. Token stocké.");
            } else {
                System.out.println("SERVICE DEBUG: Échec d'authentification : Identifiants invalides.");
            }

        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de l'authentification.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service.");
            e.printStackTrace();
        } 
        
        return authenticatedUser; 
    }
    
    /**
     * Logique métier pour l'inscription d'un nouvel utilisateur.
     * @param newUser L'objet User contenant username, email et psw.
     * @return true si la ressource a été créée en base de données, sinon false.
     */
    public boolean register(User newUser) {
        System.out.println("SERVICE DEBUG: Tentative d'inscription pour " + newUser.getEmail());
        
        Connection conn = null;
        boolean isCreated = false;
        
        try {
            // 1. Obtention de la connexion via le Singleton
            conn = getConnection(); 
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return false;
            }

            // 2. Instanciation du DAO (le DAO utilise les colonnes USERNAME, EMAIL, PSW)
            UserDAO userDAO = new UserDAO(conn); 
            
            // 3. Appel au DAO pour l'insertion
            // Le DAO doit utiliser la procédure stockée pour le hachage du mot de passe
            isCreated = userDAO.create(newUser);
            
            if (isCreated) {
                System.out.println("SERVICE DEBUG: Inscription réussie en base de données.");
            } else {
                System.out.println("SERVICE DEBUG: Échec de l'inscription (Email peut-être déjà utilisé).");
            }

        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de l'inscription.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans le Service.");
            e.printStackTrace();
        } 
        
        return isCreated;
    }

    /**
     * Logique de Sécurité : Génère un Token JWT unique.
     */
    public String generateJwtToken(User user) {
        String payload = user.getId() + ":" + user.getEmail() + ":" + System.currentTimeMillis();
        return "fake-jwt-token-" + payload;
    }
    
    // Vous pouvez ajouter ici d'autres méthodes de service (ex: createUser, updateWishlist)
}