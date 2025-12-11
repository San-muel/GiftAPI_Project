package be.projet.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.projet.model.User;

public class UserDao {

    private static UserDao instance = null;

    // Constructeur privé (Singleton)
    private UserDao() {
    }

    // Méthode d'accès au Singleton
    public static UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }

    // Récupérer tous les utilisateurs depuis Oracle
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        
        // La requête SQL (ADAPTEZ LE NOM DE LA TABLE ICI, ex: STUDENTS, UTILISATEURS...)
        String sql = "SELECT * FROM \"User\"";

        try {
            // 1. Récupérer la connexion via votre Singleton
            Connection conn = SingletonConnection.getConnection();
            
            // 2. Préparer la requête
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // 3. Exécuter et récupérer les résultats
            ResultSet rs = ps.executeQuery();

            // 4. Parcourir les lignes du résultat
            while (rs.next()) {
                User u = new User();
                // ADAPTEZ LES NOMS DES COLONNES (doivent correspondre à votre DB Oracle)
                u.setId(rs.getInt("ID"));       
                u.setNom(rs.getString("USERNAME"));
                u.setPrenom(rs.getString("PSW"));
                
                users.add(u);
            }
            
            // Fermeture des ressources (bonne pratique)
            rs.close();
            ps.close();
            // On ne ferme pas 'conn' ici car c'est un Singleton partagé

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    
 // Méthode pour récupérer un user par ID
    public User getUserById(int id) {
        User user = null;
        String sql = "SELECT * FROM USERS WHERE ID = ?"; // Adaptez le nom de la table

        try {
            java.sql.Connection conn = SingletonConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setNom(rs.getString("NOM"));
                user.setPrenom(rs.getString("PRENOM"));
            }
            rs.close();
            ps.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Méthode pour créer un user
    public int createUser(User user) {
        int generatedId = 0;
        // Supposons que l'ID est auto-incrémenté ou géré par une séquence Oracle
        String sql = "INSERT INTO USERS (NOM, PRENOM) VALUES (?, ?)"; 
        // Si vous utilisez une séquence Oracle manuellement : 
        // String sql = "INSERT INTO USERS (ID, NOM, PRENOM) VALUES (SEQ_USERS.NEXTVAL, ?, ?)";

        try {
            java.sql.Connection conn = SingletonConnection.getConnection();
            // On demande de récupérer les clés générées (l'ID)
            java.sql.PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            
            ps.executeUpdate();

            // Récupération de l'ID généré par Oracle
            java.sql.ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                // Oracle retourne souvent le ROWID ou la clé générée
                generatedId = rs.getInt(1); 
            }
            ps.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }
}