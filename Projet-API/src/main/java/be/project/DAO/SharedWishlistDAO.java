package be.project.DAO;

import java.sql.Connection;
import java.util.List;

import be.project.model.SharedWishlist;

public class SharedWishlistDAO extends AbstractDAO<SharedWishlist> {

    public SharedWishlistDAO(Connection connect) {
        super(connect);  
    }

    @Override
    public boolean create(SharedWishlist obj) {
        // TODO : INSERT INTO shared_wishlist (wishlist_id, user_id, shared_at, notification) VALUES (...)
        return false;
    }

    @Override
    public boolean delete(SharedWishlist obj) {
        // TODO : DELETE FROM shared_wishlist WHERE id = ?
        return false;
    }

    @Override
    public boolean update(SharedWishlist obj) {
        // TODO : UPDATE shared_wishlist SET notification = ? WHERE id = ?
        return false;
    }

    @Override
    public SharedWishlist find(int id) {
        // TODO : SELECT * FROM shared_wishlist WHERE id = ?
        return null;
    }

    @Override
    public List<SharedWishlist> findAll() {
        // TODO : SELECT * FROM shared_wishlist
        return null;
    }
}