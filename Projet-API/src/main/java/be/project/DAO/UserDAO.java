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

// Supposons que AbstractDAO est une classe générique qui gère la connexion
public class UserDAO extends AbstractDAO<User> {

    public UserDAO(Connection connection) {
        super(connection);
    }

    /**
     * Authentification de l'utilisateur et hydratation complète de l'objet User.
     * Appelle pkg_user_auth.authenticate.
     *
     * @param email Email de l'utilisateur.
     * @param plainPassword Mot de passe en clair.
     * @return L'objet User HYDRATÉ si l'authentification réussit, sinon null.
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
                    
                    // Chargement des relations (Wishlists, Gifts) après authentification
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
    
    //----------------------------------------------------------------------
    // Chargement des Gifts pour une Wishlist
    //----------------------------------------------------------------------

    /**
     * Charge tous les objets Gift associés à une Wishlist donnée.
     * Utilise la procédure stockée pkg_wishlist_data.get_gifts.
     *
     * @param wishlist La Wishlist à hydrater avec les Gifts.
     */
    public void loadWishlistGifts(Wishlist wishlist) {
        if (wishlist == null || wishlist.getId() == 0) return;

        String sql = "{call pkg_wishlist_data.get_gifts(?, ?)}";
        
        int giftCount = 0;

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
                    
                    // Gérer l'Integer Priority qui peut être NULL en base
                    int priorityValue = rs.getInt("PRIORITY");
                    if (!rs.wasNull()) {
                         gift.setPriority(priorityValue);
                    } else {
                         gift.setPriority(null);
                    }
                    
                    gift.setPhotoUrl(rs.getString("PHOTO_URL"));
                    gift.setwishlist(wishlist); 
                    
                    wishlist.getGifts().add(gift); 
                    giftCount++;
                    
                    System.out.println("DAO DEBUG: Gift chargé pour Wishlist " + wishlist.getId() + " -> " + gift.getName());
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur lors du chargement des Gifts pour la Wishlist " + wishlist.getId());
            e.printStackTrace();
        }
        System.out.println("DAO DEBUG: " + giftCount + " Gifts chargés pour Wishlist " + wishlist.getId());
    }
    
    //----------------------------------------------------------------------
    // Chargement de toutes les relations de l'utilisateur
    //----------------------------------------------------------------------

    /**
     * Charge les collections de l'utilisateur (Wishlists créées, partagées, infos, et leurs Gifts).
     * Utilise la procédure stockée pkg_user_data.load_user_data.
     */
    public void loadUserRelations(User user) {
        if (user == null || user.getId() == 0) return;

        // La procédure stockée retourne 4 REF CURSORs
        String sql = "{call pkg_user_data.load_user_data(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, user.getId());

            cs.registerOutParameter(2, OracleTypes.CURSOR); // Wishlists créées
            cs.registerOutParameter(3, OracleTypes.CURSOR); // Wishlists partagées
            cs.registerOutParameter(4, OracleTypes.CURSOR); // Infos de partage
            cs.registerOutParameter(5, OracleTypes.CURSOR); // Contributions

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

                    // HYDRATATION : Chargement des Gifts pour cette Wishlist
                    this.loadWishlistGifts(wl);
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
                    
                    // HYDRATATION : Chargement des Gifts pour cette Wishlist
                    this.loadWishlistGifts(wl);
                }
            }
            System.out.println("DAO DEBUG: " + sharedCount + " Wishlists Partagées chargées.");

            // 3. Infos de partage (Liaison entre la Wishlist et les détails du partage)
            int infoCount = 0;
            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                while (rs.next()) {
                    SharedWishlist sw = new SharedWishlist();
                    
                    // On utilise WISHLIST_ID comme ID pour l'objet SharedWishlist
                    sw.setId(rs.getInt("WISHLIST_ID")); 
                    
                    if (rs.getTimestamp("SHARED_AT") != null) {
                        sw.setSharedAt(rs.getTimestamp("SHARED_AT").toLocalDateTime());
                    }
                    
                    sw.setNotification(rs.getString("NOTIFICATION"));
                    
                    // Ajout à la collection de l'utilisateur
                    user.getSharedWishlistInfos().add(sw);
                    infoCount++;
                    
                    System.out.println("DAO DEBUG: Info de partage ajoutée pour la Wishlist ID: " + sw.getId());
                }
            }
            System.out.println("DAO DEBUG: " + infoCount + " objets SharedWishlist créés.");
            // 4. Contributions (Non implémenté ici, mais le curseur est géré)

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur lors du chargement des relations pour l'utilisateur " + user.getId());
            e.printStackTrace();
        }
    }
    
    //----------------------------------------------------------------------
    // Implémentations des méthodes héritées (pour la complétude)
    //----------------------------------------------------------------------
    
    @Override
    public boolean create(User user) {
        // Appel de la procédure stockée via le package
        String sql = "{call pkg_user_auth.register_user(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            // Paramètres d'entrée (IN)
            cs.setString(1, user.getUsername());
            cs.setString(2, user.getEmail().trim().toLowerCase());
            cs.setString(3, user.getPsw()); // Le mot de passe en clair

            // Paramètres de sortie (OUT)
            cs.registerOutParameter(4, Types.VARCHAR); // p_success
            cs.registerOutParameter(5, Types.VARCHAR); // p_error_msg

            cs.execute();

            String successStr = cs.getString(4);
            String errorMsg = cs.getString(5);

            if ("TRUE".equalsIgnoreCase(successStr)) {
                System.out.println("DAO DEBUG: Inscription réussie pour " + user.getEmail());
                return true;
            } else {
                System.err.println("DAO DEBUG: Échec inscription : " + errorMsg);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur lors de l'appel à pkg_user_auth.register_user");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(User obj) {
        // Logique de suppression (à implémenter)
        return false;
    }

    @Override
    public boolean update(User obj) {
        // Logique de mise à jour (à implémenter)
        return false;
    }

    @Override
    public User find(int id) {
        // Logique de recherche par ID (à implémenter)
        return null;
    }

    @Override
    public List<User> findAll() {
        // Logique de recherche de tous les utilisateurs (à implémenter)
        return null;
    }
}