package be.project.DAO;

import be.project.model.*;
import be.project.model.Contribution;
import be.project.model.Gift;
import be.project.model.SharedWishlist;
import be.project.model.Wishlist;
import be.project.singleton.SingletonConnection;

public class DAOFactory extends AbstractDAOFactory {

    @Override
    public DAO<User> getUserDAO() {
        return new UserDAO(SingletonConnection.getConnection());
    }

    @Override
    public DAO<Wishlist> getWishlistDAO() {
        return new WishlistDAO(SingletonConnection.getConnection());
    }

    @Override
    public DAO<Gift> getGiftDAO() {
        return new GiftDAO(SingletonConnection.getConnection());
    }

    @Override
    public DAO<Contribution> getContributionDAO() {
        return new ContributionDAO(SingletonConnection.getConnection());
    }

    @Override
    public DAO<SharedWishlist> getSharedWishlistDAO() {
        return new SharedWishlistDAO(SingletonConnection.getConnection());
    }
}