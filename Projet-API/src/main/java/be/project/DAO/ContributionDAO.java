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
        String sql = "{call PKG_CONTRIBUTION_DATA.add_contribution(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, obj.getUserId());
            cs.setInt(2, obj.getGiftId());
            cs.setDouble(3, obj.getAmount());
            cs.setString(4, obj.getComment());
            
            cs.registerOutParameter(5, Types.INTEGER); 
            cs.registerOutParameter(6, Types.INTEGER); 

            cs.execute();
            
            int newId = cs.getInt(5);      
            int statusCode = cs.getInt(6); 
            
            if (statusCode == 1) {
                obj.setId(newId);
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

    public List<Contribution> findAllByGiftId(int giftId) {
        List<Contribution> list = new ArrayList<>();
        String sql = "{call PKG_CONTRIBUTION_DATA.get_contributions_by_gift(?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setInt(1, giftId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
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
            cs.setInt(1, id);
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
        return new ArrayList<>(); 
    }

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
    
    @Override public boolean delete(Contribution obj) { return false; }
    @Override public boolean update(Contribution obj) { return false; }
}