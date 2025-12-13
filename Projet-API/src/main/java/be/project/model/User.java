package be.project.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User implements Serializable {

	private static final long serialVersionUID = 5297121229532248788L;
	private int id;
	private String username;
	private String email;
	private String psw;
	
    // NOUVEAU CHAMP POUR LE TOKEN JWT
    private String token; 
    
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

    // NOUVEAU GETTER POUR LE TOKEN
    public String getToken() {
        return token;
    }

    // NOUVEAU SETTER POUR LE TOKEN
    // Le UserService/UserAPI l'utilisera pour insérer le token avant de renvoyer le JSON.
    public void setToken(String token) {
        this.token = token;
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
        User user = (User) o;  
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}