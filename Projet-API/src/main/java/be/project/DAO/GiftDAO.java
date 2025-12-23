package be.project.DAO;

import be.project.model.Gift;
import be.project.singleton.SingletonConnection;
import oracle.jdbc.OracleTypes; 
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class GiftDAO extends AbstractDAO<Gift> {

    public GiftDAO(Connection connection) {
        super(connection);
    }

    private Connection getActiveConnection() throws SQLException {
        return SingletonConnection.getConnection();
    }

    public Gift create(Gift gift, int wishlistId, int userId) {
        System.out.println("API DEBUG: Entrée dans GiftDAO.create");
        System.out.println("API DEBUG: Params -> WishlistId: " + wishlistId + ", UserId: " + userId + ", Name: " + gift.getName());

        String sql = "{call pkg_gift_data.create_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, wishlistId);
            cs.setInt(2, userId);
            cs.setString(3, gift.getName());
            cs.setString(4, gift.getDescription());
            cs.setDouble(5, gift.getPrice());
            cs.setObject(6, gift.getPriority(), Types.INTEGER);
            cs.setString(7, gift.getPhotoUrl());
            cs.registerOutParameter(8, Types.INTEGER); 
            cs.registerOutParameter(9, Types.INTEGER); 

            System.out.println("API DEBUG: Exécution de la procédure stockée...");
            cs.execute();
            
            int generatedId = cs.getInt(8);
            int statusCode = cs.getInt(9); 
            System.out.println("API DEBUG: Procédure terminée. Status: " + statusCode + ", New ID: " + generatedId);

            if (statusCode == 1) {
                conn.commit(); 
                System.out.println("API DEBUG: COMMIT réussi.");
                gift.setId(generatedId);
                return gift;
            } else {
                System.err.println("API DEBUG: La procédure a retourné un code d'échec.");
            }
        } catch (SQLException e) { 
            System.err.println("API DEBUG: ERREUR SQL dans GiftDAO.create");
            e.printStackTrace(); 
        }
        return null;
    }

    public boolean update(Gift gift, int wishlistId, int userId) {
        String sql = "{call pkg_gift_data.modify_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, gift.getId());
            cs.setInt(2, wishlistId);
            cs.setInt(3, userId);
            cs.setString(4, gift.getName());
            cs.setString(5, gift.getDescription());
            cs.setDouble(6, gift.getPrice());
            cs.setObject(7, gift.getPriority(), Types.INTEGER);
            cs.setString(8, gift.getPhotoUrl());
            cs.registerOutParameter(9, Types.INTEGER); 

            cs.execute();
            if (cs.getInt(9) == 1) {
                conn.commit();
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int giftId, int userId) {
        String sql = "{call pkg_gift_data.delete_gift(?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, giftId); 
            cs.setInt(2, userId);
            cs.registerOutParameter(3, Types.INTEGER); 
            cs.execute();
            if (cs.getInt(3) == 1) {
                conn.commit();
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Gift> getAllGiftsForUser(int userId) {
        List<Gift> gifts = new ArrayList<>();
        String sql = "{call pkg_gift_data.get_all_user_gifts(?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Gift g = new Gift();
                    g.setId(rs.getInt("GIFT_ID"));
                    g.setName(rs.getString("NAME"));
                    g.setDescription(rs.getString("DESCRIPTION"));
                    g.setPrice(rs.getDouble("PRICE"));
                    g.setPriority(rs.getInt("PRIORITY"));
                    g.setPhotoUrl(rs.getString("PHOTO_URL"));
                    gifts.add(g);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return gifts;
    }
    
    /**
     * Récupère tous les cadeaux d'une liste spécifique via un curseur Oracle.
     * Utilisé par WishlistDAO.find(id) pour peupler la liste de cadeaux.
     */
    public java.util.Set<Gift> findAllByWishlistId(int wishlistId) {
        java.util.Set<Gift> gifts = new java.util.HashSet<>();
        // Procédure PL/SQL pour récupérer les cadeaux d'une wishlist
        String sql = "{call pkg_gift_data.get_gifts_by_wishlist(?, ?)}";

        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, wishlistId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            System.out.println("[API GiftDAO] Chargement des cadeaux pour la wishlist : " + wishlistId);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Gift g = new Gift();
                    // On utilise les noms de colonnes de ta table GIFT
                    g.setId(rs.getInt("ID")); 
                    g.setName(rs.getString("NAME"));
                    g.setDescription(rs.getString("DESCRIPTION"));
                    g.setPrice(rs.getDouble("PRICE"));
                    g.setPriority(rs.getInt("PRIORITY"));
                    g.setPhotoUrl(rs.getString("PHOTO_URL"));             
                    gifts.add(g);
                }
            }
            System.out.println("[API GiftDAO] Nombre de cadeaux récupérés : " + gifts.size());
        } catch (SQLException e) {
            System.err.println("[ERREUR API GiftDAO] findAllByWishlistId: " + e.getMessage());
            e.printStackTrace();
        }
        return gifts;
    }

    @Override public Gift find(int id) { return null; }
    @Override public List<Gift> findAll() { return new ArrayList<>(); }
    @Override public boolean delete(Gift obj) { return false; } 
    @Override public boolean update(Gift obj) { return false; }
    @Override public boolean create(Gift obj) { return false; }
}