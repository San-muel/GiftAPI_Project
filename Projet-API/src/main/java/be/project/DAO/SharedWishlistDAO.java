package be.project.DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import be.project.model.SharedWishlist;

public class SharedWishlistDAO extends AbstractDAO<SharedWishlist> {

    public SharedWishlistDAO(Connection connect) {
        super(connect);  
    }

    /**
     * Utilise la procédure stockée pkg_shared_wishlist.share_list
     */
    public boolean createWithTarget(SharedWishlist obj, int targetUserId) {
        // Appel de la procédure stockée (5 paramètres : 3 IN, 2 OUT)
        String sql = "{call pkg_shared_wishlist.share_list(?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = connection.prepareCall(sql)) {
            // Paramètres d'entrée
            cs.setInt(1, obj.getId()); // ID de la wishlist
            cs.setInt(2, targetUserId);
            cs.setString(3, obj.getNotification());

            // Paramètres de sortie
            cs.registerOutParameter(4, Types.VARCHAR); // p_success
            cs.registerOutParameter(5, Types.VARCHAR); // p_error_msg

            cs.execute();

            String success = cs.getString(4);
            if ("TRUE".equalsIgnoreCase(success)) {
                return true;
            } else {
                System.err.println("DAO SharedWishlist Error: " + cs.getString(5));
                return false;
            }
        } catch (SQLException e) {
            System.err.println("DAO SharedWishlist SQLException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean create(SharedWishlist obj) {
        // On n'utilise pas cette méthode car il manque l'ID de l'utilisateur cible
        return false;
    }

    @Override
    public boolean delete(SharedWishlist obj) { return false; }

    @Override
    public boolean update(SharedWishlist obj) { return false; }

    @Override
    public SharedWishlist find(int id) { return null; }

    @Override
    public List<SharedWishlist> findAll() { return null; }
}