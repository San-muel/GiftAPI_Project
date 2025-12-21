package be.project.model;

import be.project.DAO.SharedWishlistDAO;
import be.project.singleton.SingletonConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class SharedWishlist implements Serializable {

    private static final long serialVersionUID = -2811572698812735751L;
    private int id; // Ici, l'ID de la wishlist concernée (comme dans ton UserDAO)
    private LocalDateTime sharedAt;  
    private String notification;         

    public SharedWishlist() {}

    /**
     * Méthode Active Record pour créer le lien.
     * Les IDs sont passés en paramètres car ils appartiennent à la relation en DB,
     * même si l'objet SharedWishlist final ne les "possède" pas tous les deux.
     */
    public static boolean createLink(int wishlistId, int targetUserId, String notification) throws SQLException {
        try (Connection conn = SingletonConnection.getConnection()) {
            if (conn == null) return false;

            // On crée l'objet d'info pour le partage
            SharedWishlist sw = new SharedWishlist();
            sw.setId(wishlistId); // On suit ta logique : l'ID de SharedWishlist = ID de la Wishlist
            sw.setNotification(notification);

            SharedWishlistDAO dao = new SharedWishlistDAO(conn);
            // On passe l'ID de l'utilisateur cible au DAO séparément
            return dao.createWithTarget(sw, targetUserId);
        }
    }

    // Getters et Setters classiques
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