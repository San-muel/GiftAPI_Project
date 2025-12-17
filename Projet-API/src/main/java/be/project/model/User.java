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
    private String token; 
    
    // On utilise exactement les mêmes noms de champs que ceux attendus par le Client
    private Set<Contribution> contributions = new HashSet<>();
    private Set<Wishlist> sharedWishlists = new HashSet<>();   // Corrigé
    private Set<Wishlist> createdWishlists = new HashSet<>();  // Corrigé
    private Set<SharedWishlist> sharedWishlistInfos = new HashSet<>(); // Corrigé
    private Set<SharedWishlist> infoWishlist = new HashSet<>(); // Ajouté pour compatibilité

    public User() {}
    
    // --- Getters et Setters standard (Noms alignés sur les variables) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPsw() { return psw; }
    public void setPsw(String psw) { this.psw = psw; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Set<Contribution> getContributions() { return contributions; }
    public void setContributions(Set<Contribution> contributions) { this.contributions = contributions; }

    public Set<Wishlist> getSharedWishlists() { return sharedWishlists; }
    public void setSharedWishlists(Set<Wishlist> sharedWishlists) { this.sharedWishlists = sharedWishlists; }

    public Set<Wishlist> getCreatedWishlists() { return createdWishlists; }
    public void setCreatedWishlists(Set<Wishlist> createdWishlists) { this.createdWishlists = createdWishlists; }

    public Set<SharedWishlist> getSharedWishlistInfos() { return sharedWishlistInfos; }
    public void setSharedWishlistInfos(Set<SharedWishlist> sharedWishlistInfos) { this.sharedWishlistInfos = sharedWishlistInfos; }

    public Set<SharedWishlist> getInfoWishlist() { return infoWishlist; }
    public void setInfoWishlist(Set<SharedWishlist> infoWishlist) { this.infoWishlist = infoWishlist; }

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