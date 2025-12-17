package be.project.DAO;

import be.project.model.Gift;
import oracle.jdbc.OracleTypes; 
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

// Supposons que AbstractDAO est une classe générique qui gère la connexion
public class GiftDAO extends AbstractDAO<Gift> {

    public GiftDAO(Connection connection) {
        super(connection);
    }
    
    // =========================================================================
    // CRUD : CREATE
    // =========================================================================
    public Gift create(Gift gift, int wishlistId, int userId) {
        
        // 9 paramètres : 7 IN, 1 OUT (ID), 1 OUT (Status)
        String sql = "{call pkg_gift_data.create_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            // 1. IN parameters (Positions 1 à 7)
            cs.setInt(1, wishlistId);
            cs.setInt(2, userId); // Utilisé pour vérifier les droits d'insertion
            cs.setString(3, gift.getName());
            cs.setString(4, gift.getDescription());
            cs.setDouble(5, gift.getPrice());
            
            if (gift.getPriority() != null) {
                cs.setInt(6, gift.getPriority());
            } else {
                cs.setNull(6, Types.INTEGER);
            }
            
            cs.setString(7, gift.getPhotoUrl());

            // 2. OUT parameters
            // 8. p_new_gift_id
            cs.registerOutParameter(8, Types.INTEGER); 
            // 9. p_status_code
            cs.registerOutParameter(9, Types.INTEGER); 

            cs.execute();
            
            // 3. Récupération des valeurs de sortie
            int generatedId = cs.getInt(8);
            int statusCode = cs.getInt(9); 

            if (statusCode == 1 && generatedId > 0) {
                gift.setId(generatedId);
                System.out.println("DAO DEBUG: Nouveau Gift inséré. ID: " + generatedId);
                return gift;
            } else {
                System.err.println("DAO ERROR: Insertion Gift échouée. Statut retourné: " + statusCode + ", ID: " + generatedId);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur SQL lors de l'appel à pkg_gift_data.create_gift");
            e.printStackTrace();
            return null;
        }
    }
    
    // =========================================================================
    // CRUD : UPDATE
    // =========================================================================
    public boolean update(Gift gift, int wishlistId, int userId) {
        
    	System.out.println("DAO CHECK: Appel modify_gift avec GiftID=" + gift.getId() + ", WLID=" + wishlistId + ", UserID=" + userId);
        
        // 9 paramètres : 8 IN, 1 OUT (Status)
        String sql = "{call pkg_gift_data.modify_gift(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            // 1. IN parameters
            cs.setInt(1, gift.getId()); // ID du cadeau à modifier
            cs.setInt(2, wishlistId);
            cs.setInt(3, userId); // Utilisé pour vérifier les droits de modification
            cs.setString(4, gift.getName());
            cs.setString(5, gift.getDescription());
            cs.setDouble(6, gift.getPrice());
            
            if (gift.getPriority() != null) {
                cs.setInt(7, gift.getPriority());
            } else {
                cs.setNull(7, Types.INTEGER);
            }
            
            cs.setString(8, gift.getPhotoUrl());

            // 2. OUT parameter (Statut de l'opération: 1=Succès, 0=Échec/Non autorisé)
            cs.registerOutParameter(9, Types.INTEGER); 

            cs.execute();
            
            // 3. Récupération du statut
            int resultStatus = cs.getInt(9);

            if (resultStatus == 1) {
                System.out.println("DAO DEBUG: Gift ID " + gift.getId() + " mis à jour avec succès.");
                return true;
            } else {
                System.err.println("DAO ERROR: Mise à jour Gift échouée (statut retourné: " + resultStatus + 
                                   "). Vérifiez l'autorisation et l'existence du cadeau.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur SQL lors de l'appel à pkg_gift_data.modify_gift");
            e.printStackTrace();
            return false;
        }
    }
    
    // =========================================================================
    // CRUD : DELETE (Méthode spécifique à l'API)
    // =========================================================================
    /**
     * Supprime un cadeau de la base de données via la procédure stockée delete_gift.
     * @param giftId L'ID du cadeau à supprimer.
     * @param userId L'ID de l'utilisateur qui exécute la suppression (pour l'autorisation).
     * @return true si la suppression a réussi, false sinon (y compris si non autorisé ou non trouvé).
     */
    public boolean delete(int giftId, int userId) {
        
        System.out.println("DAO CHECK: Appel delete_gift avec GiftID=" + giftId + ", UserID=" + userId);
        
        // 3 paramètres : 2 IN, 1 OUT (Status)
        String sql = "{call pkg_gift_data.delete_gift(?, ?, ?)}";

        try (CallableStatement cs = connection.prepareCall(sql)) {
            
            // 1. IN parameters
            cs.setInt(1, giftId); 
            cs.setInt(2, userId); // Utilisé pour vérifier les droits
            
            // 2. OUT parameter (Statut de l'opération: 1=Succès, 0=Échec/Non autorisé)
            cs.registerOutParameter(3, Types.INTEGER); 

            cs.execute();
            
            // 3. Récupération du statut
            int resultStatus = cs.getInt(3);

            if (resultStatus == 1) {
                System.out.println("DAO DEBUG: Suppression Gift ID " + giftId + " réussie.");
                return true;
            } else {
                System.err.println("DAO ERROR: Suppression Gift ID " + giftId + " échouée (statut retourné: " + resultStatus + ").");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("DAO ERROR: Erreur SQL lors de l'appel à pkg_gift_data.delete_gift");
            e.printStackTrace();
            return false;
        }
    }


    //----------------------------------------------------------------------
    // Implémentations des méthodes héritées
    //----------------------------------------------------------------------

    @Override
    public Gift find(int id) {
        // Logique de recherche (à implémenter)
        return null;
    }
    
    // Méthode pour la récupération de tous les cadeaux de l'utilisateur (affichage principal)
    public List<Gift> getAllGiftsForUser(int userId) {
        // *** À implémenter : utiliser un RefCursor pour récupérer tous les cadeaux
        // de toutes les wishlists (créées ou partagées) associées à cet utilisateur. ***
        System.out.println("DAO WARNING: getAllGiftsForUser est à implémenter.");
        return List.of(); // Retourne une liste vide pour l'instant
    }


    @Override
    public List<Gift> findAll() {
        return null;
    }
    
    // Surcharge des méthodes génériques non utilisées
    @Override public boolean delete(Gift obj) { return false; } // Utiliser la surcharge delete(int giftId, int userId)
    @Override public boolean update(Gift obj) { return false; } // Utiliser la surcharge update(Gift gift, int wishlistId, int userId)

	@Override
	public boolean create(Gift obj) {
		return false; // Utiliser la surcharge create(Gift gift, int wishlistId, int userId)
	}
}