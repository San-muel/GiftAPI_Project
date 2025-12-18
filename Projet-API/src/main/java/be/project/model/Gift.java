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
    private String name;
    private String description;
    private double price;
    private Integer priority;  
    private String photoUrl;
    private Set<Contribution> contributions = new HashSet<>();

    public Gift() {}

    // --- Méthodes d'Action ---

    public Gift create(int wishlistId, int userId) throws SQLException {
        // On récupère la connexion sans try-with-resources pour ne pas la fermer
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return null;
        GiftDAO dao = new GiftDAO(conn);
        return dao.create(this, wishlistId, userId);
    }

    public boolean update(int wishlistId, int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return false;
        GiftDAO dao = new GiftDAO(conn);
        return dao.update(this, wishlistId, userId);
    }

    public boolean delete(int userId) throws SQLException {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return false;
        GiftDAO dao = new GiftDAO(conn);
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

    @JsonIgnore
    public GiftStatus getStatus() {
        double totalContributed = contributions.stream().mapToDouble(Contribution::getAmount).sum();
        if (totalContributed >= price && totalContributed > 0) return GiftStatus.FUNDED;
        if (totalContributed > 0) return GiftStatus.PARTIALLY_FUNDED;
        return GiftStatus.AVAILABLE; 
    }
}