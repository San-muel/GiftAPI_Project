package be.project.DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import be.project.model.Status;
import be.project.model.Wishlist;
import oracle.jdbc.OracleTypes;

public class WishlistDAO extends AbstractDAO<Wishlist> {

    // On redéclare connect ici pour être certain qu'il soit accessible dans les méthodes
    private Connection connect;

    public WishlistDAO(Connection connect) {
        super(connect);
        this.connect = connect;
    }

    @Override
    public List<Wishlist> findAll() {
        List<Wishlist> list = new ArrayList<>();
        // On appelle la procédure stockée dans ton package Oracle
        String sql = "{call pkg_wishlist_data.get_all_wishlists(?)}"; 

        try (CallableStatement call = this.connect.prepareCall(sql)) {
            // On enregistre le paramètre de sortie (le curseur)
            call.registerOutParameter(1, OracleTypes.CURSOR); 
            call.execute();

            // On récupère le curseur et on le parcourt
            try (ResultSet rs = (ResultSet) call.getObject(1)) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean create(Wishlist obj) {
        String sql = "{call pkg_wishlist_data.create_wishlist(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement call = this.connect.prepareCall(sql)) {
            // Paramètres IN
            call.setInt(1, 101); // ID statique pour le test, à dynamiser plus tard
            call.setString(2, obj.getTitle());
            call.setString(3, obj.getOccasion());
            call.setDate(4, obj.getExpirationDate() != null ? Date.valueOf(obj.getExpirationDate()) : null);
            call.setString(5, obj.getStatus() != null ? obj.getStatus().name() : "OPEN");
            
            // Paramètre OUT (ID généré)
            call.registerOutParameter(6, Types.INTEGER);
            
            call.execute();
            
            int generatedId = call.getInt(6);
            if (generatedId > 0) {
                obj.setId(generatedId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] create: " + e.getMessage());
        }
        return false;
    }

    private Wishlist map(ResultSet rs) throws SQLException {
        Wishlist w = new Wishlist();
        w.setId(rs.getInt("ID"));
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
                w.setStatus(Status.INACTIVE); // Valeur par défaut
            }
        }
        return w;
    }

    @Override public boolean delete(Wishlist obj) { return false; }
    @Override public boolean update(Wishlist obj) { return false; }
    @Override public Wishlist find(int id) { return null; }
}