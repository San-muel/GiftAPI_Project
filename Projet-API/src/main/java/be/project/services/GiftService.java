package be.project.services;

import be.project.DAO.GiftDAO;
import be.project.model.Gift;
import be.project.singleton.SingletonConnection; 

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class GiftService {
    
    private Connection getConnection() {
        return SingletonConnection.getConnection(); 
    }

    /**
     * Logique métier pour ajouter un cadeau.
     * @param newGift L'objet Gift avec ses données.
     * @param wishlistId L'ID de la liste à laquelle le cadeau est ajouté.
     * @param userId L'ID de l'utilisateur qui fait l'action (pour l'autorisation).
     * @return L'objet Gift, hydraté avec l'ID généré par la DB, ou null en cas d'échec/autorisation.
     */
    public Gift createGift(Gift newGift, int wishlistId, int userId) {
        
        System.out.println("SERVICE DEBUG: Création Gift pour Wishlist " + wishlistId + " par User " + userId);
        
        Connection conn = null; 
        Gift createdGift = null;
        
        try {
            conn = getConnection(); 
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return null;
            }

            // 1. Instanciation du DAO
            GiftDAO giftDAO = new GiftDAO(conn); 
            
            // 2. Appel au DAO
            createdGift = giftDAO.create(newGift, wishlistId, userId); 
            
            if (createdGift != null) {
                System.out.println("SERVICE DEBUG: Cadeau créé avec succès. ID: " + createdGift.getId());
            } else {
                System.out.println("SERVICE DEBUG: Échec de l'insertion du cadeau.");
            }

        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de la création du cadeau.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service.");
            e.printStackTrace();
        } 
        
        return createdGift; 
    }

    /**
     * Logique métier pour modifier un cadeau.
     */
    public boolean updateGift(Gift modifiedGift, int wishlistId, int userId) {
        
        System.out.println("SERVICE DEBUG: Mise à jour Gift ID " + modifiedGift.getId() + 
                           " pour Wishlist " + wishlistId + " par User " + userId);
        
        Connection conn = null; 
        boolean success = false;
        
        // Validation basique
        if (modifiedGift.getId() <= 0) {
            System.err.println("SERVICE ERROR: ID du cadeau à modifier manquant ou invalide.");
            return false;
        }

        try {
            conn = getConnection(); 
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return false;
            }

            // 1. Instanciation du DAO
            GiftDAO giftDAO = new GiftDAO(conn); 
            
            // 2. Appel au DAO
            success = giftDAO.update(modifiedGift, wishlistId, userId); 
            
            if (success) {
                System.out.println("SERVICE DEBUG: Cadeau ID " + modifiedGift.getId() + " mis à jour avec succès.");
            } else {
                System.out.println("SERVICE DEBUG: Échec de la mise à jour du cadeau (Non trouvé ou Non autorisé).");
            }

        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de la mise à jour du cadeau.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service lors de la mise à jour.");
            e.printStackTrace();
        } 
        
        return success; 
    }
    
    /**
     * NOUVEAU: Logique métier pour supprimer un cadeau.
     * @param giftId L'ID du cadeau à supprimer.
     * @param userId L'ID de l'utilisateur qui fait l'action (pour l'autorisation).
     * @return true si la suppression a réussi (204 No Content), false sinon.
     */
    public boolean deleteGift(int giftId, int userId) {
        
        System.out.println("SERVICE DEBUG: Suppression Gift ID " + giftId + " par User " + userId);
        
        Connection conn = null; 
        boolean success = false;
        
        if (giftId <= 0) {
            System.err.println("SERVICE ERROR: ID du cadeau à supprimer manquant ou invalide.");
            return false;
        }

        try {
            conn = getConnection(); 
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return false;
            }

            // 1. Instanciation du DAO
            GiftDAO giftDAO = new GiftDAO(conn); 
            
            // 2. Appel au DAO (Méthode à créer dans GiftDAO de l'API)
            // On suppose que cette méthode existe et appelle la procédure stockée DELETE.
            success = giftDAO.delete(giftId, userId); 
            
            if (success) {
                System.out.println("SERVICE DEBUG: Cadeau ID " + giftId + " supprimé avec succès.");
            } else {
                System.out.println("SERVICE DEBUG: Échec de la suppression du cadeau (Non trouvé ou Non autorisé).");
            }

        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de la suppression du cadeau.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service lors de la suppression.");
            e.printStackTrace();
        } 
        
        return success; 
    }


    /**
     * Récupère tous les cadeaux des listes créées et partagées avec l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return Une liste de tous les cadeaux pertinents.
     */
    public List<Gift> getAllGiftsForUser(int userId) {
        
        System.out.println("SERVICE DEBUG: Récupération de tous les cadeaux pour User " + userId);
        
        Connection conn = null;
        
        try {
            conn = getConnection();
            
            if (conn == null || conn.isClosed()) {
                System.err.println("SERVICE ERROR: Connexion à la base de données indisponible.");
                return Collections.emptyList();
            }
            
            GiftDAO giftDAO = new GiftDAO(conn);
            
            List<Gift> gifts = giftDAO.getAllGiftsForUser(userId);
            
            return gifts;
            
        } catch (SQLException e) {
            System.err.println("SERVICE ERROR: Erreur SQL lors de la récupération des cadeaux de l'utilisateur.");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Erreur inattendue dans la couche Service lors de la lecture.");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}