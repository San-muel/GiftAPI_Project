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
    
    public Wishlist() {}
    
    public Wishlist(int id, String title, String occasion, LocalDate expirationDate, Status status) {
        this();
        this.id = id;
        this.title = title;
        this.occasion = occasion;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    private WishlistDAO dao() {
        return (WishlistDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO();
    }
    
    public Wishlist create(int userId) {
        return dao().create(this, userId);
    }

    public boolean update(int userId) {
        return dao().update(this, userId);
    }

    public boolean delete(int userId) {
        return dao().delete(this.getId(), userId);
    }

    public static List<Wishlist> getAllForUser(int userId) {
        WishlistDAO dao = (WishlistDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO();
        return dao.findAllByUserId(userId);
    }

    public static Wishlist find(int id) {
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO().find(id);
    }
    
    public static List<Wishlist> findAll() {
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getWishlistDAO().findAll();
    }

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