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

    // --- CREATE ---
    public Gift create(Gift gift, int wishlistId) {
        System.out.println("[DEBUG GiftDAO] Entrée dans create() pour: " + gift.getName());
        String sql = "{call pkg_gift_data.create_gift(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, wishlistId);
            cs.setString(2, gift.getName());
            cs.setString(3, gift.getDescription());
            cs.setDouble(4, gift.getPrice());
            cs.setInt(5, gift.getPriority() != null ? gift.getPriority() : 3);
            cs.setString(6, gift.getPhotoUrl());
            cs.setString(7, "AVAILABLE");
            cs.registerOutParameter(8, Types.INTEGER); 

            System.out.println("[DEBUG GiftDAO] Exécution procedure create_gift...");
            cs.execute();
            int generatedId = cs.getInt(8);
            System.out.println("[DEBUG GiftDAO] ID généré par Oracle: " + generatedId);

            if (generatedId > 0) {
                conn.commit(); 
                System.out.println("[DEBUG GiftDAO] Commit réussi.");
                gift.setId(generatedId);
                return gift;
            }
        } catch (SQLException e) { 
            System.out.println("[ERREUR GiftDAO] Erreur SQL dans create: " + e.getMessage());
            e.printStackTrace(); 
        }
        return null;
    }

    // --- UPDATE ---
    public boolean update(Gift gift, int wishlistId, int userId) {
        System.out.println("[DEBUG GiftDAO] Entrée dans update() pour Gift ID: " + gift.getId());
        String sql = "{call pkg_gift_data.update_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, gift.getId());
            cs.setInt(2, wishlistId);
            cs.setInt(3, userId);
            cs.setString(4, gift.getName());
            cs.setString(5, gift.getDescription());
            cs.setDouble(6, gift.getPrice());
            cs.setInt(7, gift.getPriority() != null ? gift.getPriority() : 3);
            cs.setString(8, gift.getPhotoUrl());
            cs.registerOutParameter(9, Types.INTEGER); // Status retour

            cs.execute();
            int result = cs.getInt(9);
            System.out.println("[DEBUG GiftDAO] Résultat procédure update: " + result);

            if (result == 1) {
                conn.commit();
                System.out.println("[DEBUG GiftDAO] Update committé avec succès.");
                return true;
            }
        } catch (SQLException e) { 
            System.out.println("[ERREUR GiftDAO] Erreur SQL dans update: " + e.getMessage());
            e.printStackTrace(); 
        }
        return false;
    }

    // --- DELETE ---
    public boolean delete(int giftId, int userId) {
        System.out.println("[DEBUG GiftDAO] Entrée dans delete() pour Gift ID: " + giftId);
        String sql = "{call pkg_gift_data.delete_gift(?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, giftId); 
            cs.setInt(2, userId);
            cs.registerOutParameter(3, Types.INTEGER); 
            cs.execute();
            int result = cs.getInt(3);
            System.out.println("[DEBUG GiftDAO] Résultat procédure delete: " + result);

            if (result == 1) {
                conn.commit();
                System.out.println("[DEBUG GiftDAO] Delete committé.");
                return true;
            }
        } catch (SQLException e) { 
            System.out.println("[ERREUR GiftDAO] Erreur SQL dans delete: " + e.getMessage());
            e.printStackTrace(); 
        }
        return false;
    }

    // --- GET ALL FOR USER ---
    public List<Gift> getAllGiftsForUser(int userId) {
        System.out.println("[DEBUG GiftDAO] Récupération de tous les cadeaux pour User ID: " + userId);
        List<Gift> gifts = new ArrayList<>();
        String sql = "{call pkg_gift_data.get_user_gifts(?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    gifts.add(map(rs));
                }
            }
            System.out.println("[DEBUG GiftDAO] Nombre de cadeaux trouvés: " + gifts.size());
        } catch (SQLException e) { 
            System.out.println("[ERREUR GiftDAO] Erreur dans getAllGiftsForUser: " + e.getMessage());
            e.printStackTrace(); 
        }
        return gifts;
    }

    public java.util.Set<Gift> findAllByWishlistId(int wishlistId) {
        System.out.println("[DEBUG GiftDAO] findAllByWishlistId pour ID: " + wishlistId);
        java.util.Set<Gift> gifts = new java.util.HashSet<>();
        String sql = "{call pkg_wishlist_data.get_gifts(?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, wishlistId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    gifts.add(map(rs));
                }
            }
            System.out.println("[DEBUG GiftDAO] Cadeaux trouvés dans la liste: " + gifts.size());
        } catch (SQLException e) { 
            System.out.println("[ERREUR GiftDAO] Erreur dans findAllByWishlistId: " + e.getMessage());
            e.printStackTrace(); 
        }
        return gifts;
    }

    // Mapping
    private Gift map(ResultSet rs) throws SQLException {
        Gift g = new Gift();
        int id = 0;
        try {
            id = rs.getInt("ID");
        } catch (SQLException e) {
            // Pas de print ici pour ne pas polluer si c'est GIFT_ID
            id = rs.getInt("GIFT_ID");
        }
        g.setId(id);
        g.setName(rs.getString("NAME"));
        g.setDescription(rs.getString("DESCRIPTION"));
        g.setPrice(rs.getDouble("PRICE"));
        g.setPriority(rs.getInt("PRIORITY"));
        g.setPhotoUrl(rs.getString("PHOTO_URL"));
        return g;
    }

    // Méthodes forcées
    @Override public Gift find(int id) { return null; }
    @Override public List<Gift> findAll() { return new ArrayList<>(); }
    @Override public boolean delete(Gift obj) { return false; } 
    @Override public boolean update(Gift obj) { return false; }
    @Override public boolean create(Gift obj) { return false; }
}