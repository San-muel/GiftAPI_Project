package be.project.DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import be.project.model.SharedWishlist;
import be.project.model.Status;
import be.project.model.User;
import be.project.model.Wishlist;
import oracle.jdbc.OracleTypes;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(Connection connection) {
        super(connection);
    }

    /**
     * Authentification et HYDRATATION COMPLÈTE de l'utilisateur.
     * Appelle la procédure stockée pkg_user_auth.authenticate pour vérifier
     * les identifiants, puis charge toutes ses relations (loadUserRelations).
     *
     * @param email Email de l'utilisateur.
     * @param plainPassword Mot de passe en clair.
     * @return L'objet User HYDRATÉ (avec Wishlists, etc.) si l'authentification réussit, sinon null.
     */
    public User authenticate(String email, String plainPassword) {
    	    User user = null;
    	    String sql = "{call pkg_user_auth.authenticate(?, ?, ?, ?, ?, ?, ?)}";

    	    try (CallableStatement cs = connection.prepareCall(sql)) {
    	        // IN parameters
    	        cs.setString(1, email.trim().toLowerCase());
    	        cs.setString(2, plainPassword);

    	        // OUT parameters
    	        cs.registerOutParameter(3, Types.INTEGER);   // p_user_id
    	        cs.registerOutParameter(4, Types.VARCHAR);   // p_username
    	        cs.registerOutParameter(5, Types.VARCHAR);   // p_user_email
    	        cs.registerOutParameter(6, Types.VARCHAR);   // p_success ('TRUE'/'FALSE')
    	        cs.registerOutParameter(7, Types.VARCHAR);   // p_error_msg

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
                    
                    // NOUVEAU : Chargement des relations (Wishlists, Contributions, etc.)
                    this.loadUserRelations(user); 
    	        } else {
    	            System.out.println("DAO DEBUG: Échec authentification : " + (errorMsg != null ? errorMsg : "Inconnu"));
    	        }

    	    } catch (SQLException e) {
    	        System.err.println("DAO ERROR: Erreur lors de l'appel à pkg_user_auth.authenticate");
    	        e.printStackTrace();
    	    }

    	    return user;
    	}
    
    /**
     * Charge les relations de l'utilisateur (Wishlists créées, partagées, etc.)
     * ... (Reste de la méthode inchangé, car elle est correcte) ...
     */
    public void loadUserRelations(User user) {
        if (user == null || user.getId() == 0) return;

        // La procédure stockée retourne 4 REF CURSORs
        String sql = "{call pkg_user_data.load_user_data(?, ?, ?, ?, ?)}";
        // ... (Reste du code de mapping des curseurs) ...
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, user.getId());

            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.registerOutParameter(3, OracleTypes.CURSOR);
            cs.registerOutParameter(4, OracleTypes.CURSOR);
            cs.registerOutParameter(5, OracleTypes.CURSOR);

            cs.execute();
            
            System.out.println("DAO DEBUG: Démarrage du chargement des relations pour User ID: " + user.getId());

            // 1. Wishlists créées
            int createdCount = 0;
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
                    createdCount++;
                    System.out.println("DAO DEBUG: Wishlist Créée chargée -> ID: " + wl.getId() + ", Titre: " + wl.getTitle());
                }
            }
            System.out.println("DAO DEBUG: " + createdCount + " Wishlists Créées chargées.");


            // 2. Wishlists partagées
            int sharedCount = 0;
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
                    sharedCount++;
                    System.out.println("DAO DEBUG: Wishlist Partagée chargée -> ID: " + wl.getId() + ", Titre: " + wl.getTitle());
                }
            }
            System.out.println("DAO DEBUG: " + sharedCount + " Wishlists Partagées chargées.");

            // 3. Infos de partage
            int infoCount = 0;
            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                while (rs.next()) {
                    SharedWishlist sw = new SharedWishlist();
                    sw.setId(rs.getInt("WISHLIST_ID")); 
                    
                    if (rs.getTimestamp("SHARED_AT") != null) {
                        sw.setSharedAt(rs.getTimestamp("SHARED_AT").toLocalDateTime());
                    }
                    sw.setNotification(rs.getString("NOTIFICATION"));
                    user.getSharedWishlistInfos().add(sw);
                    infoCount++;
                }
            }
            System.out.println("DAO DEBUG: " + infoCount + " Infos de Partage chargées.");


            // 4. Contributions (À implémenter si besoin d'afficher)

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur lors du chargement des relations pour l'utilisateur " + user.getId());
            e.printStackTrace();
        }
    }
    
    // ... (Autres méthodes stub) ...   
    @Override
    public boolean create(User user) {
        // Logique de création (à implémenter via procédure stockée)
        return false;
    }

    @Override
    public boolean delete(User obj) {
        // Logique de suppression (à implémenter via procédure stockée)
        return false;
    }

    @Override
    public boolean update(User obj) {
        // Logique de mise à jour (à implémenter via procédure stockée)
        return false;
    }

    @Override
    public User find(int id) {
        // Logique de recherche par ID (à implémenter via fonction/procédure)
        return null;
    }

    @Override
    public List<User> findAll() {
        // Logique de recherche de tous les utilisateurs (à implémenter via procédure/collection)
        return null;
    }
}