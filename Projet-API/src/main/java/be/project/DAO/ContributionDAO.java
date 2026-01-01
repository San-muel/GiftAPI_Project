package be.project.DAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import be.project.model.Contribution;

public class ContributionDAO extends AbstractDAO<Contribution> {

    public ContributionDAO(Connection connect) {
        super(connect);
    }

    @Override
    public boolean create(Contribution obj) {
        // CORRECTION 1 : On ajoute un paramètre de sortie pour l'ID (donc 6 points d'interrogation)
        // Assure-toi que ta procédure PL/SQL ressemble bien à : (user, gift, amount, comment, OUT_ID, OUT_STATUS)
        String sql = "{call PKG_CONTRIBUTION_DATA.add_contribution(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, obj.getUserId());
            cs.setInt(2, obj.getGiftId());
            cs.setDouble(3, obj.getAmount());
            cs.setString(4, obj.getComment());
            
            // CORRECTION 2 : Enregistrement des paramètres de sortie
            cs.registerOutParameter(5, Types.INTEGER); // p_new_id (L'ID généré)
            cs.registerOutParameter(6, Types.INTEGER); // p_status_code (Le succès/échec)

            cs.execute();
            
            int newId = cs.getInt(5);      // On récupère l'ID
            int statusCode = cs.getInt(6); // On récupère le statut
            
            if (statusCode == 1) {
                // CORRECTION 3 : IMPORTANT ! On met l'ID dans l'objet Java
                // Comme ça, l'API renverra le bon ID au client
                obj.setId(newId);
                // On met aussi la date actuelle car la BDD vient de le faire (optionnel mais mieux pour l'affichage immédiat)
                if (obj.getContributedAt() == null) {
                    obj.setContributedAt(LocalDateTime.now());
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL create contribution: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère toutes les contributions liées à un cadeau spécifique
     */
    public List<Contribution> findAllByGiftId(int giftId) {
        List<Contribution> list = new ArrayList<>();
        // Requête SQL standard (assurez-vous que vos noms de colonnes correspondent à votre BDD)
        String sql = "SELECT ID, USER_ID, GIFT_ID, AMOUNT, COMMENT_TEXT, CONTRIBUTED_AT FROM CONTRIBUTIONS WHERE GIFT_ID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, giftId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToContribution(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL findAllByGiftId: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Contribution find(int id) {
        // CORRECTION : Vraie requête SQL
        String sql = "SELECT ID, USER_ID, GIFT_ID, AMOUNT, COMMENT_TEXT, CONTRIBUTED_AT FROM CONTRIBUTIONS WHERE ID = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
             ps.setInt(1, id);
             try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToContribution(rs);
                }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Contribution> findAll() {
        // ... (Tu peux garder ta logique existante ou utiliser un simple SELECT *)
        return new ArrayList<>(); 
    }

    // --- MAPPING ---
    private Contribution mapResultSetToContribution(ResultSet rs) throws SQLException {
        Contribution c = new Contribution();
        c.setId(rs.getInt("ID"));
        c.setUserId(rs.getInt("USER_ID"));
        c.setGiftId(rs.getInt("GIFT_ID"));
        c.setAmount(rs.getDouble("AMOUNT"));
        c.setComment(rs.getString("COMMENT_TEXT"));
        
        Timestamp ts = rs.getTimestamp("CONTRIBUTED_AT");
        if (ts != null) {
            c.setContributedAt(ts.toLocalDateTime());
        }
        return c;
    }
    
    
    
    // Méthodes inutilisées
    @Override public boolean delete(Contribution obj) { return false; }
    @Override public boolean update(Contribution obj) { return false; }
}