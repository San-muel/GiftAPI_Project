package be.project.DAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import be.project.model.Contribution;

public class ContributionDAO extends AbstractDAO<Contribution> {

    public ContributionDAO(Connection connect) {
        super(connect);
    }

    @Override
    public boolean create(Contribution obj) {
        // Note: On passe l'ID de l'user et du gift contenus dans l'objet Contribution
        String sql = "{call PKG_CONTRIBUTION_DATA.add_contribution(?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, obj.getUserId());    // USER_ID
            cs.setInt(2, obj.getGiftId());    // GIFT_ID
            cs.setDouble(3, obj.getAmount()); // AMOUNT
            cs.setString(4, obj.getComment());// COMMENT_TEXT
            cs.registerOutParameter(5, Types.INTEGER); // p_status_code

            cs.execute();
            
            return cs.getInt(5) == 1; // Retourne true si status_code est 1
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'appel de la procédure: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Contribution obj) {
        return true;
    }

    @Override
    public boolean update(Contribution obj) {
        return true;
    }

    @Override
    public Contribution find(int id) {
        System.out.println(">>> DÉBUT find(" + id + ") - " + LocalDateTime.now());

        // Ici, la connexion passée dans le constructeur est utilisée
        String sql = "SELECT 1 AS ID, 9999.99 AS AMOUNT, 'Test depuis Java' AS COMMENT_TEXT FROM DUAL WHERE 1 = ?"; // Simplifié pour test
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
             stmt.setInt(1, id); // Ajout d'un paramètre pour simuler l'utilisation de l'ID
             try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Contribution c = mapResultSetToContribution(rs);
                    System.out.println(">>> Contribution trouvée : " + c);
                    System.out.println(">>> FIN find() - " + LocalDateTime.now());
                    return c;
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(">>> FIN find() SANS RÉSULTAT");
        return null;
    }


    @Override
    public List<Contribution> findAll() {
        List<Contribution> contributions = new ArrayList<>();
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.prepareCall(
                "SELECT * FROM TABLE(CONTRIBUTION_PKG.find_all_contributions())"
            );
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                contributions.add(mapResultSetToContribution(rs));
            }
            
            return contributions;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de toutes les contributions: " + e.getMessage());
            return contributions;
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
    }
    
    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Contribution
     */
    private Contribution mapResultSetToContribution(ResultSet rs) throws SQLException {
        Contribution contrib = new Contribution();
        contrib.setId(rs.getInt("ID"));
        contrib.setAmount(rs.getDouble("AMOUNT"));
        contrib.setComment(rs.getString("COMMENT_TEXT"));
        
        return contrib;
    }
    
    /**
     * Méthode utilitaire pour fermer un Statement
     */
    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Méthode utilitaire pour fermer un ResultSet
     */
    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
            }
        }
    }
}