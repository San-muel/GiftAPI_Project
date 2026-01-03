package be.project.DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import be.project.model.Gift;
import be.project.model.SharedWishlist;
import be.project.model.Status;
import be.project.model.User;
import be.project.model.Wishlist;
import oracle.jdbc.OracleTypes;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(Connection connection) {
        super(connection);
    }

    public User authenticate(String email, String plainPassword) {
    	    User user = null;
    	    String sql = "{call pkg_user_auth.authenticate(?, ?, ?, ?, ?, ?, ?)}";

    	    try (CallableStatement cs = connection.prepareCall(sql)) {
    	        cs.setString(1, email.trim().toLowerCase());
    	        cs.setString(2, plainPassword);

    	        cs.registerOutParameter(3, Types.INTEGER);   
    	        cs.registerOutParameter(4, Types.VARCHAR);   
    	        cs.registerOutParameter(5, Types.VARCHAR);   
    	        cs.registerOutParameter(6, Types.VARCHAR);   
    	        cs.registerOutParameter(7, Types.VARCHAR);   

    	        cs.execute();

    	        String successStr = cs.getString(6);
    	        String errorMsg = cs.getString(7);

    	        if ("TRUE".equalsIgnoreCase(successStr)) {
    	            user = new User();
    	            user.setId(cs.getInt(3));
    	            user.setUsername(cs.getString(4));
    	            user.setEmail(cs.getString(5));
    	            user.setPsw(null); 

    	            System.out.println("DAO DEBUG: Authentification réussie pour " + user.getEmail());
                    this.loadUserRelations(user); 
    	        } else {
    	            System.out.println("DAO DEBUG: Échec authentification : " + (errorMsg != null ? errorMsg : "Inconnu"));
    	        }

    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }
    	    return user;
    	}
    
    public void loadWishlistGifts(Wishlist wishlist) {
        if (wishlist == null || wishlist.getId() == 0) return;

        String sql = "{call pkg_wishlist_data.get_gifts(?, ?)}";
        ContributionDAO contributionDAO = new ContributionDAO(this.connection);

        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, wishlist.getId());
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Gift gift = new Gift();
                    gift.setId(rs.getInt("ID"));
                    gift.setName(rs.getString("NAME"));
                    gift.setDescription(rs.getString("DESCRIPTION"));
                    gift.setPrice(rs.getDouble("PRICE"));
                    
                    int priorityValue = rs.getInt("PRIORITY");
                    gift.setPriority(rs.wasNull() ? null : priorityValue);
                    
                    gift.setPhotoUrl(rs.getString("PHOTO_URL"));
                    gift.setSiteUrl(rs.getString("SITE_URL")); 

                    List<be.project.model.Contribution> contribs = contributionDAO.findAllByGiftId(gift.getId());
                    gift.getContributions().addAll(contribs); 

                    wishlist.getGifts().add(gift); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void loadUserRelations(User user) {
        if (user == null || user.getId() == 0) return;

        String sql = "{call pkg_user_data.load_user_data(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, user.getId());

            cs.registerOutParameter(2, OracleTypes.CURSOR); 
            cs.registerOutParameter(3, OracleTypes.CURSOR); 
            cs.registerOutParameter(4, OracleTypes.CURSOR); 
            cs.registerOutParameter(5, OracleTypes.CURSOR); 

            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Wishlist wl = new Wishlist();
                    wl.setId(rs.getInt("ID"));
                    wl.setTitle(rs.getString("TITLE"));
                    wl.setOccasion(rs.getString("OCCASION"));
                    if (rs.getDate("EXPIRATION_DATE") != null) {
                        wl.setExpirationDate(rs.getDate("EXPIRATION_DATE").toLocalDate());
                    }
                    wl.setStatus(Status.valueOf(rs.getString("STATUS")));
                    user.getCreatedWishlists().add(wl); 
                    this.loadWishlistGifts(wl);
                }
            }

            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                while (rs.next()) {
                    Wishlist wl = new Wishlist();
                    wl.setId(rs.getInt("ID"));
                    wl.setTitle(rs.getString("TITLE"));
                    wl.setOccasion(rs.getString("OCCASION"));
                    if (rs.getDate("EXPIRATION_DATE") != null) {
                        wl.setExpirationDate(rs.getDate("EXPIRATION_DATE").toLocalDate());
                    }
                    wl.setStatus(Status.valueOf(rs.getString("STATUS")));
                    user.getSharedWishlists().add(wl);
                    this.loadWishlistGifts(wl);
                }
            }

            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                while (rs.next()) {
                    SharedWishlist sw = new SharedWishlist();
                    sw.setId(rs.getInt("WISHLIST_ID")); 
                    if (rs.getTimestamp("SHARED_AT") != null) {
                        sw.setSharedAt(rs.getTimestamp("SHARED_AT").toLocalDateTime());
                    }
                    sw.setNotification(rs.getString("NOTIFICATION"));
                    user.getSharedWishlistInfos().add(sw);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean create(User user) {
        String sql = "{call pkg_user_auth.register_user(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, user.getUsername());
            cs.setString(2, user.getEmail().trim().toLowerCase());
            cs.setString(3, user.getPsw());

            cs.registerOutParameter(4, java.sql.Types.VARCHAR); 
            cs.registerOutParameter(5, java.sql.Types.VARCHAR); 

            cs.execute();
            
            String successStr = cs.getString(4);
            return "TRUE".equalsIgnoreCase(successStr);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(User obj) {
        return false;
    }

    @Override
    public boolean update(User obj) {
        return false;
    }

    @Override
    public User find(int id) {
        List<User> allUsers = this.findAll();
        for (User u : allUsers) {
            if (u.getId() == id) {
                this.loadUserRelations(u);
                return u;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT ID, USERNAME, EMAIL FROM \"User\" ORDER BY USERNAME";
        
        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("ID")); 
                u.setUsername(rs.getString("USERNAME"));
                u.setEmail(rs.getString("EMAIL"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("DAO ERROR UserDAO.findAll: " + e.getMessage());
        }
        return users;
    }
}