package be.project.DAO;

import be.project.model.Status;
import be.project.model.Wishlist;
import be.project.singleton.SingletonConnection;
import oracle.jdbc.OracleTypes; 

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO extends AbstractDAO<Wishlist> {

    public WishlistDAO() {
        super(SingletonConnection.getConnection());
    }

    public WishlistDAO(Connection connection) {
        super(connection);
    }

    private Connection getActiveConnection() throws SQLException {
        return SingletonConnection.getConnection();
    }

    public Wishlist create(Wishlist wishlist, int userId) {
        String sql = "{call pkg_wishlist_data.create_wishlist(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getActiveConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, userId);
            cs.setString(2, wishlist.getTitle());
            cs.setString(3, wishlist.getOccasion());
            if (wishlist.getExpirationDate() != null) {
                cs.setDate(4, java.sql.Date.valueOf(wishlist.getExpirationDate()));
            } else {
                cs.setNull(4, Types.DATE);
            }
            cs.setString(5, wishlist.getStatus() != null ? wishlist.getStatus().name() : "ACTIVE");
            cs.registerOutParameter(6, Types.INTEGER); 
            cs.registerOutParameter(7, Types.INTEGER); 
            cs.execute();
            if (cs.getInt(7) == 1) {
                conn.commit(); 
                wishlist.setId(cs.getInt(6));
                return wishlist;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean update(Wishlist wishlist, int userId) {
        String sql = "{call pkg_wishlist_data.update_wishlist(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, wishlist.getId());
            cs.setInt(2, userId); 
            cs.setString(3, wishlist.getTitle());
            cs.setString(4, wishlist.getOccasion());
            
            if (wishlist.getExpirationDate() != null) {
                cs.setDate(5, java.sql.Date.valueOf(wishlist.getExpirationDate()));
            } else {
                cs.setNull(5, java.sql.Types.DATE);
            }

            String statusValue = wishlist.getStatus() != null ? wishlist.getStatus().toString() : "ACTIVE";
            cs.setString(6, statusValue);
            
            cs.registerOutParameter(7, java.sql.Types.INTEGER);

            cs.execute();
            
            int result = cs.getInt(7);

            if (result >= 1) { 
                conn.commit();
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public List<Wishlist> findAllByUserId(int userId) {
        List<Wishlist> wishlists = new ArrayList<>();
        String sql = "{call pkg_wishlist_data.get_user_wishlists(?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    wishlists.add(map(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return wishlists;
    }

    @Override
    public Wishlist find(int id) {
        Wishlist wishlist = null;
        String sql = "{call pkg_wishlist_data.get_wishlist_by_id(?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                if (rs.next()) {
                    wishlist = map(rs); 
                    GiftDAO giftDAO = new GiftDAO(conn);
                    wishlist.setGifts(giftDAO.findAllByWishlistId(id));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return wishlist;
    }

    @Override
    public List<Wishlist> findAll() {
        List<Wishlist> list = new ArrayList<>();
        String sql = "{call pkg_wishlist_data.get_all_wishlists(?)}"; 
        try (Connection conn = getActiveConnection();
             CallableStatement call = conn.prepareCall(sql)) {
            call.registerOutParameter(1, OracleTypes.CURSOR); 
            call.execute();
            try (ResultSet rs = (ResultSet) call.getObject(1)) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Wishlist map(ResultSet rs) throws SQLException {
        Wishlist w = new Wishlist();
        int id;
        try { id = rs.getInt("WISHLIST_ID"); } catch (SQLException e) { id = rs.getInt("ID"); }
        w.setId(id);
        w.setTitle(rs.getString("TITLE"));
        w.setOccasion(rs.getString("OCCASION"));
        java.sql.Date dbDate = rs.getDate("EXPIRATION_DATE");
        if (dbDate != null) w.setExpirationDate(dbDate.toLocalDate());
        String statusStr = rs.getString("STATUS");
        if (statusStr != null) {
            try { w.setStatus(Status.valueOf(statusStr.toUpperCase())); } 
            catch (IllegalArgumentException e) { w.setStatus(Status.ACTIVE); }
        }
        return w;
    }

    public boolean delete(int wishlistId, int userId) {
        String sql = "{call pkg_wishlist_data.delete_wishlist(?, ?, ?)}";
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, wishlistId); 
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

    @Override public boolean delete(Wishlist obj) { return false; } 
    @Override public boolean update(Wishlist obj) { return false; }
    @Override public boolean create(Wishlist obj) { return false; }
}