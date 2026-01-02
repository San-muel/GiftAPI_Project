package be.project.model;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.SharedWishlistDAO;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class SharedWishlist implements Serializable {

    private static final long serialVersionUID = -2811572698812735751L;
    private int id; 
    private LocalDateTime sharedAt;  
    private String notification;         

    public SharedWishlist() {}

    /**
     * Méthode Active Record pour créer le lien.
     * Nettoyée : Plus de connexion SQL ici, on passe par la Factory.
     */
    public static boolean createLink(int wishlistId, int targetUserId, String notification) throws Exception {
        // 1. On prépare l'objet (Active Record)
        SharedWishlist sw = new SharedWishlist();
        sw.setId(wishlistId); 
        sw.setNotification(notification);

        // 2. On récupère le DAO via la Factory
        // On doit caster en (SharedWishlistDAO) car on utilise une méthode spécifique 'createWithTarget'
        SharedWishlistDAO dao = (SharedWishlistDAO) AbstractDAOFactory
                                    .getFactory(AbstractDAOFactory.JDBC_DAO)
                                    .getSharedWishlistDAO();

        // 3. On délègue l'exécution au DAO
        // Le DAO s'occupe de récupérer la connexion via SingletonConnection en interne
        return dao.createWithTarget(sw, targetUserId);
    }

    // --- Getters et Setters classiques ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
    
    public String getNotification() { return notification; }
    public void setNotification(String notification) { this.notification = notification; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedWishlist that = (SharedWishlist) o;
        return id == that.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}