package be.project.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Wishlist implements Serializable {

	private static final long serialVersionUID = 7031345627323684647L;
	private int id;
    private String title;
    private String occasion;         
    private LocalDate expirationDate;
    private Status status; 
    private User owner;
    private Set<User> sharedWithUsers = new HashSet<>();
    private Set<Gift> gifts = new HashSet<>();
    
    public Wishlist() {}
    
    public Wishlist(int id, String title, String occasion, LocalDate expirationDate,
    			Status  status, User owner) {
		this();
		this.id = id;
		this.title = title;
		this.occasion = occasion;
		this.expirationDate = expirationDate;
		this.status = status;
		this.owner = owner;
	}
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

    public Status  getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getSharedWithUsers() {
        return sharedWithUsers;
    }

    public void setSharedWithUsers(Set<User> sharedWithUsers) {
        this.sharedWithUsers = sharedWithUsers;
    }

    public Set<Gift> getGifts() {
        return gifts;
    }

    public void setGifts(Set<Gift> gifts) {
        this.gifts = gifts;
    }
    
    @Override
    public String toString() {
        return "Wishlist{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", occasion='" + occasion + '\'' +
                ", owner=" + (owner != null ? owner.getUsername() : "null") +
                ", status='" + status + '\'' +
                ", nbGifts=" + gifts.size() +
                ", nbSharedWith=" + sharedWithUsers.size() +
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
