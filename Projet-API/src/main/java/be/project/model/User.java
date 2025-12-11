package be.project.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User {

	private int id;
	private String username;
	private String email;
	private String psw;
	private Set<Contribution> contributions = new HashSet<>();
	private Set<Wishlist> WishlistPartager = new HashSet<>();
	private Set<Wishlist> WishlistCreer = new HashSet<>();
	private Set<SharedWishlist> InfoWishlist = new HashSet<>();
	
    public User() {}
    
    public User(int id, String username, String email, String psw) {
        this();
        this.id = id;
        this.username = username;
        this.email = email;
        this.psw = psw;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public Set<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(Set<Contribution> contributions) {
        this.contributions = contributions;
    }

    public Set<Wishlist> getCreatedWishlists() {
        return WishlistCreer;
    }

    public void setCreatedWishlists(Set<Wishlist> createdWishlists) {
        this.WishlistCreer = createdWishlists;
    }

    public Set<Wishlist> getSharedWishlists() {
        return WishlistPartager;
    }

    public void setSharedWishlists(Set<Wishlist> sharedWishlists) {
        this.WishlistPartager = sharedWishlists;
    }

    public Set<SharedWishlist> getSharedWishlistInfos() {
        return InfoWishlist;
    }

    public void setSharedWishlistInfos(Set<SharedWishlist> sharedWishlistInfos) {
        this.InfoWishlist = sharedWishlistInfos;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;  // ← C'était Wishlist avant ! Erreur grave corrigée
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
