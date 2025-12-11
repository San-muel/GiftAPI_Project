package be.project.DAO;

import java.sql.Connection;
import java.util.List;

import be.project.model.Wishlist;

public class WishlistDAO extends AbstractDAO<Wishlist> {

    public WishlistDAO(Connection connect) {
        super(connect);
    }

    @Override
    public boolean create(Wishlist obj) {
        // À implémenter plus tard
        return false;
    }

    @Override
    public boolean delete(Wishlist obj) {
        return false;
    }

    @Override
    public boolean update(Wishlist obj) {
        return false;
    }

    @Override
    public Wishlist find(int id) {
        return null;
    }

    @Override
    public List<Wishlist> findAll() {
        return null;
    }
}