package be.project.model;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.UserDAO;
import java.io.Serializable;
import java.sql.SQLException; // On garde juste l'exception pour la signature
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

    // --- LE RACCOURCI (Méthode privée pour les instances) ---
    private UserDAO dao() {
        return (UserDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getUserDAO();
    }

    // --- Méthodes Active Record ---

    public static User authenticate(String email, String psw) throws SQLException {
        // 1. Appel à la Factory (plus de connexion manuelle)
        UserDAO dao = (UserDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getUserDAO();
        
        // 2. Appel de la méthode spécifique du DAO
        User user = dao.authenticate(email, psw);
        
        // 3. Logique métier (Token) reste ici
        if (user != null) {
            user.setToken(user.generateJwtToken());
        }
        return user;
    }

    public boolean register() throws SQLException {
        // Nettoyé : on utilise le raccourci dao()
        // Plus de try/catch SQL ici, c'est le DAO qui gère ou propage
        return dao().create(this);
    }

    public static List<User> getAllUsers() throws SQLException {
        // Appel direct à la Factory
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getUserDAO().findAll();
    }

    public static User findById(int id) throws SQLException {
        // Appel direct à la Factory
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getUserDAO().find(id);
    }

    /**
     * Génère un Token JWT (Logique de sécurité)
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

    @Override
    public int hashCode() { return Objects.hash(id); }
}