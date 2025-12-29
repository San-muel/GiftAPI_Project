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
        // Changement : 9 points d'interrogation maintenant
        String sql = "{call pkg_gift_data.create_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, wishlistId);
            cs.setString(2, gift.getName());
            cs.setString(3, gift.getDescription());
            cs.setDouble(4, gift.getPrice());
            cs.setInt(5, gift.getPriority() != null ? gift.getPriority() : 3);
            cs.setString(6, gift.getPhotoUrl());
            cs.setString(7, gift.getSiteUrl()); // <--- NOUVEAU (7ème)
            cs.setString(8, "AVAILABLE");       // (8ème)
            cs.registerOutParameter(9, Types.INTEGER); // ID généré (9ème)

            cs.execute();
            int generatedId = cs.getInt(9);

            if (generatedId > 0) {
                conn.commit(); 
                gift.setId(generatedId);
                return gift;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // --- UPDATE ---
    public boolean update(Gift gift, int wishlistId, int userId) {
        // Changement : 10 points d'interrogation maintenant
        String sql = "{call pkg_gift_data.update_gift(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
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
            cs.setString(9, gift.getSiteUrl()); // <--- NOUVEAU (9ème)
            cs.registerOutParameter(10, Types.INTEGER); // Status retour (10ème)

            cs.execute();
            int result = cs.getInt(10);

            if (result == 1) {
                conn.commit();
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    // --- DELETE ---
    public boolean delete(int giftId, int userId) {
        String sql = "{call pkg_gift_data.delete_gift(?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, giftId); 
            cs.setInt(2, userId);
            cs.registerOutParameter(3, Types.INTEGER); 
            cs.execute();
            int result = cs.getInt(3);

            if (result == 1) {
                conn.commit();
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    // --- GET ALL FOR USER (Côté SERVEUR API) ---
    public List<Gift> getAllGiftsForUser(int userId) {
        System.out.println("\n[DEBUG SERVEUR DAO] --- Récupération des cadeaux pour User: " + userId + " ---");
        List<Gift> gifts = new ArrayList<>();
        String sql = "{call pkg_gift_data.get_user_gifts(?, ?)}";
        
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            System.out.println("[DEBUG SERVEUR DAO] Exécution de pkg_gift_data.get_user_gifts...");
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    Gift g = map(rs); // Utilise ta méthode map qui contient rs.getString("SITE_URL")
                    
                    // --- PRINT DE VÉRIFICATION ---
                    System.out.println("[DEBUG SERVEUR DAO] Cadeau #" + count + " trouvé: " + g.getName());
                    System.out.println("[DEBUG SERVEUR DAO] -> Site URL en DB: " + g.getSiteUrl());
                    
                    gifts.add(g);
                }
                System.out.println("[DEBUG SERVEUR DAO] Total cadeaux récupérés: " + count);
            }
        } catch (SQLException e) { 
            System.err.println("[ERREUR SERVEUR DAO] Erreur dans getAllGiftsForUser: " + e.getMessage());
            e.printStackTrace(); 
        }
        System.out.println("[DEBUG SERVEUR DAO] --- FIN RÉCUPÉRATION ---\n");
        return gifts;
    }

    public java.util.Set<Gift> findAllByWishlistId(int wishlistId) {
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
        } catch (SQLException e) { 
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
            id = rs.getInt("GIFT_ID");
        }
        g.setId(id);
        g.setName(rs.getString("NAME"));
        g.setDescription(rs.getString("DESCRIPTION"));
        g.setPrice(rs.getDouble("PRICE"));
        g.setPriority(rs.getInt("PRIORITY"));
        g.setPhotoUrl(rs.getString("PHOTO_URL"));
        g.setSiteUrl(rs.getString("SITE_URL")); // <--- NE PAS OUBLIER LE MAPPING
        return g;
    }

    @Override public Gift find(int id) { return null; }
    @Override public List<Gift> findAll() { return new ArrayList<>(); }
    @Override public boolean delete(Gift obj) { return false; } 
    @Override public boolean update(Gift obj) { return false; }
    @Override public boolean create(Gift obj) { return false; }
}