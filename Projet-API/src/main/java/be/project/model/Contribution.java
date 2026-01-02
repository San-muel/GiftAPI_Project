package be.project.model;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.ContributionDAO; // Import du DAO spécifique
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Contribution implements Serializable {

    private static final long serialVersionUID = -1788323929082771022L;
    private int id;
    private int userId; 
    private int giftId;
    private double amount;
    private LocalDateTime contributedAt;
    private String comment;
    private Set<User> users = new HashSet<>();
    
    public Contribution() {}

    public Contribution(int id, double amount, LocalDateTime contributedAt, String comment) {
        this();
        this.id = id;
        this.amount = amount;
        this.contributedAt = contributedAt;
        this.comment = comment;
    }

    // --- LE RACCOURCI (Méthode privée pour les instances) ---
    private ContributionDAO dao() {
        return (ContributionDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getContributionDAO();
    }

    // --- Active Record (Méthodes liées à l'instance) ---

    public boolean save() {
        // Plus propre : on utilise le raccourci dao()
        // On suppose que les userId et giftId sont déjà set dans 'this'
        return dao().create(this); 
    }

    // --- Méthodes Statiques (Recherche) ---
    // Pour les méthodes static, on appelle la Factory directement car 'this.dao()' n'existe pas

    public static Contribution find(int id) {
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getContributionDAO().find(id);
    }

    public static List<Contribution> findAll() {
        return AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getContributionDAO().findAll();
    }
    
    public static List<Contribution> findAllByGiftId(int giftId) {
        // C'est ici que tu avais la connexion "en dur". C'est corrigé :
        ContributionDAO dao = (ContributionDAO) AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO).getContributionDAO();
        return dao.findAllByGiftId(giftId);
    }

    // --- Getters / Setters Standards ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif");
        }
        this.amount = amount;
    }

    public LocalDateTime getContributedAt() { return contributedAt; }
    public void setContributedAt(LocalDateTime contributedAt) { this.contributedAt = contributedAt; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
    public void addUser(User user) { this.users.add(user); }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getGiftId() { return giftId; }
    public void setGiftId(int giftId) { this.giftId = giftId; }

    @Override
    public String toString() {
        return "Contribution{" +
                "id=" + id +
                ", amount=" + amount +
                ", contributedAt=" + contributedAt +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contribution that = (Contribution) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}