package be.project.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.project.model.User;
// L'importation de SingletonConnection n'est plus nécessaire car le DAO ne l'appelle plus.
// import be.project.singleton.SingletonConnection; 

// 1. HÉRITER de AbstractDAO<User> pour utiliser la connexion héritée
public class UserDAO extends AbstractDAO<User> {

    // 2. SUPPRIMER l'implémentation du Singleton (instance, constructeur privé, getInstance())

    // 3. AJOUTER le constructeur requis par AbstractDAO
    /**
     * Le DAOFactory appelle ce constructeur pour injecter la connexion.
     */
    public UserDAO(Connection connection) {
        super(connection); // Appelle le constructeur de AbstractDAO pour initialiser this.connection
    }
    
    // 4. Implémenter toutes les méthodes abstraites de DAO<T>

    @Override
    public boolean create(User user) {
        // Le code de createUser devient create. On utilise this.connection.
        String sql = "INSERT INTO USERS (NOM) VALUES (?)"; 
        boolean success = false;

        // Utilisation de try-with-resources pour fermer automatiquement les ressources
        try (PreparedStatement ps = this.connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getUsername());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                success = true;
                // Tentative de récupération de l'ID généré
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }
    
    @Override
    public boolean delete(User obj) {
        // Implémentation de base pour respecter l'interface
        String sql = "DELETE FROM USERS WHERE ID = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, obj.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(User obj) {
        // Implémentation de base pour respecter l'interface
        String sql = "UPDATE USERS SET USERNAME = ? WHERE ID = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, obj.getUsername());
            ps.setInt(2, obj.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User find(int id) {
        User user = null;
        // Le code de getUserById devient find. On utilise this.connection.
        String sql = "SELECT ID, NOM FROM USERS WHERE ID = ?"; 

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("ID"));
                    // Attention: "NOM" ou "USERNAME"? Assurez-vous que le nom de colonne est cohérent.
                    user.setUsername(rs.getString("NOM")); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        // Le code de getAllUsers devient findAll. On utilise this.connection.
        String sql = "SELECT ID, USERNAME FROM \"User\"";

        try (PreparedStatement ps = this.connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("ID"));       
                u.setUsername(rs.getString("USERNAME"));
                
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}