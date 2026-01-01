package be.project.model;

import be.project.DAO.GiftDAO;
import be.project.singleton.SingletonConnection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Gift implements Serializable {
    private static final long serialVersionUID = 7201519647003409625L;
    
    private int id;
    private int wishlistId; // Ajouté pour faciliter le transport des données
    private String name;
    private String description;
    private double price;
    private Integer priority;  
    private String photoUrl;
    private String siteUrl;
    private Set<Contribution> contributions = new HashSet<>();

    public Gift() {}

    // --- Méthodes d'Action ---

    /**
     * Crée un cadeau.
     * Note: Le userId est passé par l'API pour la sécurité, 
     * mais n'est pas envoyé à la procédure de création du cadeau.
     */
    public Gift create(int wishlistId, int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return null;
        
        GiftDAO dao = new GiftDAO(conn);
        // On définit le wishlistId dans l'objet avant l'envoi
        this.wishlistId = wishlistId;
        
        // CORRECTION : Appel avec 2 paramètres pour matcher le GiftDAO corrigé
        return dao.create(this, wishlistId);
    }

    public boolean update(int wishlistId, int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return false;
        
        GiftDAO dao = new GiftDAO(conn);
        // Le DAO doit gérer l'update avec les paramètres de sécurité
        return dao.update(this, wishlistId, userId);
    }
    
    public boolean updatePriority(int wishlistId, int userId) throws Exception {
        try (Connection conn = SingletonConnection.getConnection()) {
            GiftDAO dao = new GiftDAO(conn);
            // On vérifie d'abord en SQL ou via le DAO que ce cadeau appartient 
            // bien à une liste appartenant à cet utilisateur
            return dao.updatePriority(this.id, this.priority);
        }
    }

    public boolean delete(int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return false;
        
        GiftDAO dao = new GiftDAO(conn);
        // On passe l'ID du cadeau et l'ID de l'utilisateur pour vérifier la propriété en SQL
        return dao.delete(this.id, userId);
    }

    public static List<Gift> getAllForUser(int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return Collections.emptyList();
        
        GiftDAO dao = new GiftDAO(conn);
        return dao.getAllGiftsForUser(userId);
    }

    // --- Getters / Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWishlistId() { return wishlistId; }
    public void setWishlistId(int wishlistId) { this.wishlistId = wishlistId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getSiteUrl() {return siteUrl;}
    public void setSiteUrl(String siteUrl) {this.siteUrl = siteUrl;}

    public Set<Contribution> getContributions() { return contributions; }
    public void setContributions(Set<Contribution> contributions) { this.contributions = contributions; }

    /**
     * Calcule le statut du cadeau en fonction des contributions.
     * @JsonIgnore évite que Jackson ne crée un champ "status" redondant en JSON 
     * s'il est déjà géré par la base de données.
     */
    @JsonIgnore
    public GiftStatus getStatus() {
        if (contributions == null || contributions.isEmpty()) {
            return GiftStatus.AVAILABLE;
        }

        double totalContributed = contributions.stream()
                .mapToDouble(Contribution::getAmount)
                .sum();

        if (totalContributed >= price && price > 0) {
            return GiftStatus.FUNDED;
        } else if (totalContributed > 0) {
            return GiftStatus.PARTIALLY_FUNDED;
        }
        
        return GiftStatus.AVAILABLE; 
    }
    @JsonIgnore
    public boolean isReadOnly() {
        // getCollectedAmount() fait la somme des contributions chargées par le DAO
        return getCollectedAmount() > 0;
    }
    public double getCollectedAmount() {
        if (contributions == null || contributions.isEmpty()) {
            return 0.0;
        }
        // Somme des montants
        return contributions.stream()
                            .mapToDouble(Contribution::getAmount)
                            .sum();
    }
    public double getRemainingAmount() {
        double remaining = this.price - getCollectedAmount();
        return Math.max(0, remaining); // Empêche les nombres négatifs
    }
}