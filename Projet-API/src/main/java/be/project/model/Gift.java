package be.project.model;

import java.io.Serializable;
import java.util.HashSet;
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
    private Wishlist wishlist;

    public Gift() {}

    public Gift(int id, String name, String description, double price, 
                Integer priority, String photoUrl,Wishlist wishlist) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.priority = priority;
        this.photoUrl = photoUrl;
        this.wishlist = wishlist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public void setwishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    public Wishlist getwishlist() {
        return wishlist;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Set<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(Set<Contribution> contributions) {
        this.contributions = contributions;
    }
    public void addContribution(Contribution contribution) {
        this.contributions.add(contribution);
    }

    public GiftStatus getStatus() {
        double totalContributed = contributions.stream()
                .mapToDouble(Contribution::getAmount)
                .sum();

        if (totalContributed >= price && totalContributed > 0) {
            return GiftStatus.FUNDED;
        } else if (totalContributed > 0) {
            return GiftStatus.PARTIALLY_FUNDED;
        } else {
            return GiftStatus.AVAILABLE; // ou RESERVED si tu gères la réservation plus tard
        }
    }

    @Override
    public String toString() {
        return "Gift{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", status=" + getStatus();
    }
}