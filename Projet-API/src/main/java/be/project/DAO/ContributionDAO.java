package be.project.DAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;

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
        // Syntaxe pour appeler la procédure avec le curseur en sortie
        String sql = "{call PKG_CONTRIBUTION_DATA.get_contributions_by_gift(?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            // Paramètre 1 : ID du cadeau (Entrée)
            cs.setInt(1, giftId);
            
            // Paramètre 2 : Le curseur de résultat (Sortie)
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            cs.execute();
            
            // On récupère le curseur et on le cast en ResultSet
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    // On utilise votre méthode existante pour mapper les colonnes
                    list.add(mapResultSetToContribution(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL findAllByGiftId (Procedure): " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Contribution find(int id) {
        String sql = "{call PKG_CONTRIBUTION_DATA.get_contribution_by_id(?, ?)}";
        
        try (CallableStatement cs = connection.prepareCall(sql)) {
            // Paramètre 1 : ID de la contribution (Entrée)
            cs.setInt(1, id);
            
            // Paramètre 2 : Le curseur de résultat (Sortie)
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                if (rs.next()) {
                    return mapResultSetToContribution(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL find contribution (Procedure): " + e.getMessage());
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