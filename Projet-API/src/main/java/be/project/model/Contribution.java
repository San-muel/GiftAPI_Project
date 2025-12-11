package be.project.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import be.project.DAO.AbstractDAOFactory;
import be.project.DAO.DAO;
// Les imports be.project.DAO.ContributionDAO et be.project.singleton.SingletonConnection
// ne sont plus nécessaires car l'accès est délégué à la Factory.

public class Contribution {

    private int id;
    private double amount;
    private LocalDateTime contributedAt;
    private String comment;
    private Gift gift;
    private Set<User> users = new HashSet<>();
    
    public Contribution() {}

    public Contribution(int id, double amount, LocalDateTime contributedAt, String comment, Gift gift) {
        this();
        this.id = id;
        this.amount = amount;
        this.contributedAt = contributedAt;
        this.comment = comment;
        this.gift = gift;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif");
        }
        this.amount = amount;
    }

    public LocalDateTime getContributedAt() {
        return contributedAt;
    }

    public void setContributedAt(LocalDateTime contributedAt) {
        this.contributedAt = contributedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    public void addUser(User user) {
        this.users.add(user);
    }

    @Override
    public String toString() {
        return "Contribution{" +
                "id=" + id +
                ", amount=" + amount +
                ", contributedAt=" + contributedAt +
                ", comment='" + comment + '\'' +
                ", giftId=" + (gift != null ? gift.getId() : "null") +
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
    
    /**
     * Méthode utilitaire pour obtenir le DAO de Contribution via la DAOFactory.
     * Cette méthode délègue la création du DAO à la Factory, respectant ainsi le découplage.
     */
    private static DAO<Contribution> getContributionDAO() {
        // Obtient la Factory concrète (ici, la Factory JDBC) en utilisant la méthode statique
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory(AbstractDAOFactory.JDBC_DAO);
        
        if (factory == null) {
            throw new IllegalStateException("Impossible d'obtenir la DAO Factory pour le type JDBC.");
        }
        
        // Demande à la Factory de fournir le DAO de Contribution.
        // C'est la Factory qui se charge de créer le DAO avec la bonne connexion.
        return factory.getContributionDAO();
    }
    
    // --- Méthodes d'accès aux données (Le rôle du Modèle) ---
    
    public static Contribution find(int id) {
        // Le Modèle appelle la méthode utilitaire qui utilise la Factory
        DAO<Contribution> dao = getContributionDAO();
        return dao.find(id);
    }

    public static List<Contribution> findAll() {
        // Le Modèle appelle la méthode utilitaire qui utilise la Factory
        DAO<Contribution> dao = getContributionDAO();
        return dao.findAll();
    }

}