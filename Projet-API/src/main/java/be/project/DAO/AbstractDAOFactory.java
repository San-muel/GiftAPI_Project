package be.project.DAO;

import be.project.model.User;
import be.project.model.Contribution;
import be.project.model.Gift;
import be.project.model.SharedWishlist;
import be.project.model.Wishlist;

public abstract class AbstractDAOFactory {

    public static final int JDBC_DAO = 1;

    public abstract DAO<User> getUserDAO();
    public abstract DAO<Wishlist> getWishlistDAO();
    public abstract DAO<Gift> getGiftDAO();
    public abstract DAO<Contribution> getContributionDAO();
    public abstract DAO<SharedWishlist> getSharedWishlistDAO();

    public static AbstractDAOFactory getFactory(int type) {
        return switch (type) {
            case JDBC_DAO -> new DAOFactory();
            default -> null;
        };
    }
}