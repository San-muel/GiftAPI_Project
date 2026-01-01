package be.project.model;

import be.project.DAO.UserDAO;
import be.project.singleton.SingletonConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class User implements Serializable {
    private static final long serialVersionUID = 5297121229532248788L;
    private int id;
    private String username;
    private String email;
    private String psw;
    private String token; 
    
    
    private Set<Contribution> contributions = new HashSet<>();
    private Set<Wishlist> sharedWishlists = new HashSet<>();   
    private Set<Wishlist> createdWishlists = new HashSet<>();  
    private Set<SharedWishlist> sharedWishlistInfos = new HashSet<>(); 

    public User() {}

    // --- Méthodes Active Record (Anciennement dans UserService) ---

    public static User authenticate(String email, String psw) throws SQLException {
        try (Connection conn = SingletonConnection.getConnection()) {
            if (conn == null) return null;
            
            UserDAO userDAO = new UserDAO(conn);
            User user = userDAO.authenticate(email, psw);
            
            if (user != null) {
                user.setToken(user.generateJwtToken());
            }
            return user;
        }
    }

    public boolean register() throws SQLException {
        System.out.println("[API-MODEL] Tentative d'obtention de la connexion DB...");
        try (Connection conn = SingletonConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[API-MODEL] ÉCHEC : La connexion DB est NULL !");
                return false;
            }
            System.out.println("[API-MODEL] Connexion DB obtenue avec succès.");
            UserDAO userDAO = new UserDAO(conn);
            return userDAO.create(this);
        } catch (SQLException e) {
            System.err.println("[API-MODEL] Erreur SQL : " + e.getMessage());
            throw e;
        }
    }

    public static List<User> getAllUsers() throws SQLException {
        try (Connection conn = SingletonConnection.getConnection()) {
            if (conn == null) return List.of();
            
            UserDAO userDAO = new UserDAO(conn);
            return userDAO.findAll(); // Appelle le DAO pour récupérer la liste
        }
    }
    /**
     * Génère un Token JWT (Logique de sécurité déplacée dans le modèle)
     */
    public String generateJwtToken() {
        String payload = this.id + ":" + this.email + ":" + System.currentTimeMillis();
        return "fake-jwt-token-" + payload;
    }

    // --- Getters et Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPsw() { return psw; }
    public void setPsw(String psw) { this.psw = psw; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Set<Wishlist> getCreatedWishlists() { return createdWishlists; }
    public Set<Wishlist> getSharedWishlists() { return sharedWishlists; }
    public Set<SharedWishlist> getSharedWishlistInfos() { return sharedWishlistInfos; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;  
        return id == user.id;
    }
    public static User findById(int id) throws SQLException {
        try (Connection conn = SingletonConnection.getConnection()) {
            if (conn == null) return null;
            
            UserDAO userDAO = new UserDAO(conn);
            User user = userDAO.find(id); // Appel du DAO
            
            return user;
        }
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}