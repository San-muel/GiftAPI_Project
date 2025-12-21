package be.project.model;

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
    
    public Wishlist(int id, String title, String occasion, LocalDate expirationDate,
                Status status) {
        this();
        this.id = id;
        this.title = title;
        this.occasion = occasion;
        this.expirationDate = expirationDate;
        this.status = status;
    }
    
    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Gift> getGifts() {
        return gifts;
    }

    public void setGifts(Set<Gift> gifts) {
        this.gifts = gifts;
    }
    
    // --- Méthodes Métier (CRUD via DAO) ---
    // Ces méthodes font le lien entre l'API et le DAO

    /**
     * Récupère toutes les listes d'un utilisateur spécifique.
     */
    public static List<Wishlist> getAllForUser(int userId) {
        WishlistDAO dao = new WishlistDAO();
        return dao.findAllByUserId(userId);
    }

    /**
     * Crée la liste actuelle en base de données pour l'utilisateur donné.
     * @return L'objet Wishlist complet avec l'ID généré.
     */
    public Wishlist create(int userId) {
        WishlistDAO dao = new WishlistDAO();
        return dao.create(this, userId);
    }

    /**
     * Met à jour la liste actuelle en base de données.
     * @return true si succès.
     */
    public boolean update(int userId) {
        WishlistDAO dao = new WishlistDAO();
        return dao.update(this, userId);
    }

    /**
     * Supprime la liste actuelle de la base de données.
     * @return true si succès.
     */
    public boolean delete(int userId) {
        WishlistDAO dao = new WishlistDAO();
        return dao.delete(this.getId(), userId);
    }

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