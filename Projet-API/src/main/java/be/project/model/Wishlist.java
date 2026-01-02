package be.project.model;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.WishlistDAO;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Wishlist implements Serializable {

    private static final long serialVersionUID = 7031345627323684647L;
    private int id;
    private String title;
    private String occasion;         
    private LocalDate expirationDate;
    private Status status; 
    private Set<Gift> gifts = new HashSet<>();
    
    // --- Constructeurs ---
    
    public Wishlist() {}
    
    public Wishlist(int id, String title, String occasion, LocalDate expirationDate, Status status) {
        this();
        this.id = id;
        this.title = title;
        this.occasion = occasion;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    // --- LE RACCOURCI (Méthode privée pour les instances) ---
    private WishlistDAO dao() {
        return (WishlistDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO();
    }
    
    // --- Méthodes Métier (Active Record) ---

    /**
     * Crée la liste actuelle en base de données pour l'utilisateur donné.
     */
    public Wishlist create(int userId) {
        // On utilise le raccourci dao()
        return dao().create(this, userId);
    }

    /**
     * Met à jour la liste actuelle en base de données.
     */
    public boolean update(int userId) {
        return dao().update(this, userId);
    }

    /**
     * Supprime la liste actuelle de la base de données.
     */
    public boolean delete(int userId) {
        return dao().delete(this.getId(), userId);
    }

    // --- Méthodes Statiques (Recherche) ---

    /**
     * Récupère toutes les listes d'un utilisateur spécifique.
     */
    public static List<Wishlist> getAllForUser(int userId) {
        // Appel direct à la Factory + Cast car findAllByUserId est spécifique à WishlistDAO
        WishlistDAO dao = (WishlistDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO();
        return dao.findAllByUserId(userId);
    }

    public static Wishlist find(int id) {
        // Appel standard via la Factory
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO().find(id);
    }
    
    /**
     * Récupère toutes les listes de la base de données (Sans filtre user).
     */
    public static List<Wishlist> findAll() {
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO().findAll();
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOccasion() { return occasion; }
    public void setOccasion(String occasion) { this.occasion = occasion; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Set<Gift> getGifts() { return gifts; }
    public void setGifts(Set<Gift> gifts) { this.gifts = gifts; }
    
    // --- Overrides ---

    @Override
    public String toString() {
        return "Wishlist{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", occasion='" + occasion + '\'' +
                ", status='" + status + '\'' +
                ", nbGifts=" + (gifts != null ? gifts.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wishlist wishlist = (Wishlist) o;
        return id == wishlist.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}