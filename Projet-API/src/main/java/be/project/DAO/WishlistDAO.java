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

    // Constructeurs
    public WishlistDAO() {
        super(SingletonConnection.getConnection());
    }

    public WishlistDAO(Connection connection) {
        super(connection);
    }

    private Connection getActiveConnection() throws SQLException {
        // On récupère la connexion via le Singleton
        return SingletonConnection.getConnection();
    }

    /**
     * Crée une wishlist en base via procédure stockée.
     * Nécessite un userId pour lier la liste.
     */
    public Wishlist create(Wishlist wishlist, int userId) {
        System.out.println("API DEBUG: Entrée dans WishlistDAO.create");
        
        String sql = "{call pkg_wishlist_data.create_wishlist(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = getActiveConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
               
               // DEBUG LOG
               System.out.println("DEBUG SQL INSERT: ID User=" + userId + 
                                  ", Titre=" + wishlist.getTitle() + 
                                  ", Date=" + wishlist.getExpirationDate() + 
                                  ", Status=" + (wishlist.getStatus() != null ? wishlist.getStatus().name() : "NULL"));

            cs.setInt(1, userId);
            cs.setString(2, wishlist.getTitle());
            cs.setString(3, wishlist.getOccasion());
            
            // Conversion LocalDate (Java) -> Date (SQL)
            if (wishlist.getExpirationDate() != null) {
                cs.setDate(4, java.sql.Date.valueOf(wishlist.getExpirationDate()));
            } else {
                cs.setNull(4, Types.DATE);
            }
            
            // Envoi du statut sous forme de String (ex: "ACTIVE")
            cs.setString(5, wishlist.getStatus() != null ? wishlist.getStatus().name() : "ACTIVE");
            
            // Paramètres de sortie
            cs.registerOutParameter(6, Types.INTEGER); // ID généré
            cs.registerOutParameter(7, Types.INTEGER); // Status code (1 = succès)

            System.out.println("API DEBUG: Exécution de pkg_wishlist_data.create_wishlist...");
            cs.execute();
            
            int generatedId = cs.getInt(6);
            int statusCode = cs.getInt(7); 
            
            System.out.println("API DEBUG: Retour procédure -> Status: " + statusCode + ", ID: " + generatedId);

            if (statusCode == 1) {
                conn.commit(); 
                wishlist.setId(generatedId);
                return wishlist;
            } else {
                System.err.println("API DEBUG: Échec création Wishlist (Code BD != 1)");
            }
        } catch (SQLException e) { 
            System.err.println("API DEBUG: ERREUR SQL dans WishlistDAO.create");
            e.printStackTrace(); 
        }
        return null;
    }

    /**
     * Met à jour une wishlist.
     */
    public boolean update(Wishlist wishlist, int userId) {
        String sql = "{call pkg_wishlist_data.update_wishlist(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, wishlist.getId());
            cs.setInt(2, userId); // Sécurité : on vérifie que c'est bien le propriétaire
            cs.setString(3, wishlist.getTitle());
            cs.setString(4, wishlist.getOccasion());
            
            if (wishlist.getExpirationDate() != null) {
                cs.setDate(5, java.sql.Date.valueOf(wishlist.getExpirationDate()));
            } else {
                cs.setNull(5, Types.DATE);
            }
            
            cs.setString(6, wishlist.getStatus() != null ? wishlist.getStatus().name() : "ACTIVE");
            
            cs.registerOutParameter(7, Types.INTEGER); // Status code

            cs.execute();
            
            if (cs.getInt(7) == 1) {
                conn.commit();
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    /**
     * Supprime une wishlist.
     */
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
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    /**
     * Récupère toutes les wishlists d'un utilisateur spécifique.
     */
    public List<Wishlist> findAllByUserId(int userId) {
        List<Wishlist> wishlists = new ArrayList<>();
        String sql = "{call pkg_wishlist_data.get_user_wishlists(?, ?)}";
        
        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR); // curseur de sortie
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Wishlist w = new Wishlist();
                    w.setId(rs.getInt("WISHLIST_ID")); 
                    w.setTitle(rs.getString("TITLE"));
                    w.setOccasion(rs.getString("OCCASION"));
                    
                    // Conversion Date SQL -> LocalDate
                    java.sql.Date dbDate = rs.getDate("EXPIRATION_DATE");
                    if (dbDate != null) {
                        w.setExpirationDate(dbDate.toLocalDate());
                    }
                    
                    // Conversion String -> Enum
                    String statusStr = rs.getString("STATUS");
                    if (statusStr != null) {
                        try {
                            w.setStatus(Status.valueOf(statusStr));
                        } catch (IllegalArgumentException ex) {
                            w.setStatus(Status.ACTIVE); // Valeur par défaut si erreur
                        }
                    }
                    
                    wishlists.add(w);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return wishlists;
    }
 // Dans WishlistDAO.java (Serveur)

    @Override
    public List<Wishlist> findAll() {
        List<Wishlist> list = new ArrayList<>();
        String sql = "{call pkg_wishlist_data.get_all_wishlists(?)}"; 

        // Utilisation correcte du try-with-resources pour la connexion
        try (Connection conn = getActiveConnection();
             CallableStatement call = conn.prepareCall(sql)) {
            
            call.registerOutParameter(1, OracleTypes.CURSOR); 
            call.execute();

            try (ResultSet rs = (ResultSet) call.getObject(1)) {
                while (rs.next()) {
                    Wishlist w = new Wishlist();
                    // Attention à bien utiliser l'alias défini dans le PL/SQL
                    w.setId(rs.getInt("WISHLIST_ID")); 
                    w.setTitle(rs.getString("TITLE"));
                    w.setOccasion(rs.getString("OCCASION"));
                    
                    java.sql.Date dbDate = rs.getDate("EXPIRATION_DATE");
                    if (dbDate != null) {
                        w.setExpirationDate(dbDate.toLocalDate());
                    }
                    
                    String statusStr = rs.getString("STATUS");
                    if (statusStr != null) {
                        try {
                            w.setStatus(Status.valueOf(statusStr));
                        } catch (IllegalArgumentException ex) {
                            w.setStatus(Status.ACTIVE);
                        }
                    }
                    list.add(w);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO SERVEUR] findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    
    private Wishlist map(ResultSet rs) throws SQLException {
        Wishlist w = new Wishlist();
        
        // CORRECTION ICI : Utiliser WISHLIST_ID au lieu de ID
        w.setId(rs.getInt("WISHLIST_ID")); 
        
        w.setTitle(rs.getString("TITLE"));
        w.setOccasion(rs.getString("OCCASION"));

        if (rs.getDate("EXPIRATION_DATE") != null) {
            w.setExpirationDate(rs.getDate("EXPIRATION_DATE").toLocalDate());
        }

        String statusStr = rs.getString("STATUS");
        if (statusStr != null) {
            try {
                w.setStatus(Status.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                w.setStatus(Status.ACTIVE); 
            }
        }
        return w;
    }

    // --- Méthodes de l'AbstractDAO non utilisées directement ---
    // On doit les laisser car on étend AbstractDAO, mais on retourne null/false
    // car on a besoin du userId pour toutes nos opérations réelles.
    
 // Dans be.project.DAO.WishlistDAO (Côté API)

 // Dans be.project.DAO.WishlistDAO (Côté API / Serveur)

    @Override
    public Wishlist find(int id) {
        Wishlist wishlist = null;
        
        // On appelle la procédure qui renvoie les infos de la table WISHLIST pour cet ID
        String sql = "{call pkg_wishlist_data.get_wishlist_by_id(?, ?)}";

        try (Connection conn = getActiveConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            System.out.println("[API DAO] Recherche de la wishlist ID : " + id);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                if (rs.next()) {
                    // On utilise ta méthode map(rs) existante pour transformer la ligne en objet Wishlist
                    wishlist = map(rs); 
                    
                    // --- RÉCUPÉRATION DES CADEAUX (Gifts) ---
                    // Très important : On instancie le GiftDAO en lui passant la connexion actuelle
                    // pour récupérer tous les cadeaux liés à WISHLIST_ID = id.
                    GiftDAO giftDAO = new GiftDAO(conn);
                    wishlist.setGifts(giftDAO.findAllByWishlistId(id));
                    
                    System.out.println("[API DAO] Wishlist trouvée : " + wishlist.getTitle() + 
                                       " avec " + wishlist.getGifts().size() + " cadeaux.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO SERVEUR] Exception dans find(id): " + e.getMessage());
            e.printStackTrace();
        }
        return wishlist;
    }
    
    @Override public boolean delete(Wishlist obj) { return false; } 
    @Override public boolean update(Wishlist obj) { return false; }
    @Override public boolean create(Wishlist obj) { return false; }
}