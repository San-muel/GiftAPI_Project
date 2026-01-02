package be.project.model;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.GiftDAO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Gift implements Serializable {
    private static final long serialVersionUID = 7201519647003409625L;
    
    private int id;
    private int wishlistId; 
    private String name;
    private String description;
    private double price;
    private Integer priority;  
    private String photoUrl;
    private String siteUrl;
    private Set<Contribution> contributions = new HashSet<>();

    public Gift() {}
    
    // --- LE RACCOURCI MAGIQUE (Méthode utilitaire privée) ---
    private GiftDAO dao() {
        return (GiftDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getGiftDAO();
    }

    // --- Méthodes d'Action (Active Record) ---

    public Gift create(int wishlistId, int userId) throws SQLException {
        return dao().create(this, wishlistId);
    }

    public boolean update(int wishlistId, int userId) throws SQLException {
        // Plus de connexion manuelle ici, tout est délégué
        return dao().update(this, wishlistId, userId);
    }
    
    public boolean updatePriority(int wishlistId, int userId) throws Exception {
        // Le DAO gère la connexion et l'exécution
        return dao().updatePriority(this.id, this.priority);
    }

    public boolean delete(int userId) throws SQLException {
        // Simple et efficace
        return dao().delete(this.id, userId);
    }

    // --- Méthode Statique (Cas particulier) ---
    public static List<Gift> getAllForUser(int userId) throws SQLException {
        // Comme on est dans un contexte 'static', on ne peut pas utiliser 'this.dao()'
        // On appelle donc la factory directement ici.
        GiftDAO dao = (GiftDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getGiftDAO();
        
        if (dao == null) return Collections.emptyList();
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

    // --- Logique Métier (Calculs) ---

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
        return getCollectedAmount() > 0;
    }

    public double getCollectedAmount() {
        if (contributions == null || contributions.isEmpty()) {
            return 0.0;
        }
        return contributions.stream()
                            .mapToDouble(Contribution::getAmount)
                            .sum();
    }

    public double getRemainingAmount() {
        double remaining = this.price - getCollectedAmount();
        return Math.max(0, remaining);
    }
}