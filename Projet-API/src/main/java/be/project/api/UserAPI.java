package be.project.api;

import be.project.model.User;
import be.project.services.UserService;

// Importations nécessaires pour Jackson et JSON (vous devez avoir cette dépendance)
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
public class UserAPI {
    
    private final UserService userService = new UserService();
    // Instanciation de l'ObjectMapper pour gérer le mapping manuellement
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Endpoint pour l'authentification (login).
     * @param jsonPayload La chaîne JSON brute (String) envoyée par le client.
     * @return Response contenant le JSON de l'objet User complet + Token, ou un statut 401.
     */
    @POST
    @Path("/login") 
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
    // Changement de la signature : String au lieu de User
    public Response login(String jsonPayload) {
        
        String email = null;
        String psw = null;
        
        try {
            // --- ÉTAPE CRUCIALE 1 : DÉSÉRIALISATION MANUELLE (ENTRÉE) ---
            // On mappe le JSON brut à un objet User temporaire pour extraire les identifiants
            User inputUser = objectMapper.readValue(jsonPayload, User.class);
            email = inputUser.getEmail();
            psw = inputUser.getPsw();
            
        } catch (IOException e) {
            // Gérer une erreur si le JSON est mal formé
            System.err.println("API ERROR: JSON d'entrée mal formé.");
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Format JSON invalide.")
                           .build();
        }
        
        System.out.println("API DEBUG: Tentative d'authentification pour : " + email);

        // 1. Appel à la couche Service (Logique métier/DB/Sécurité)
        User authenticatedUser = userService.authenticate(email, psw); 
        
        if (authenticatedUser == null) {
            // Échec
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Identifiants invalides.")
                           .build();
        }
        
        // 2. Génération et Ajout du Token (méthode de secours si le service ne le fait pas)
        // Note: Le service le fait déjà, c'est juste pour la cohérence
        String token = userService.generateJwtToken(authenticatedUser);
        authenticatedUser.setToken(token);
        
        // --- ÉTAPE CRUCIALE 2 : SÉRIALISATION MANUELLE (SORTIE) ---
        try {
            // On mappe l'objet User complet (avec collections) à une chaîne JSON.
            // On retourne la chaîne JSON directement, en utilisant Response.ok(String)
            String jsonResponse = objectMapper.writeValueAsString(authenticatedUser);
            
            // 3. Retourne la réponse JSON sous forme de String
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
            
        } catch (IOException e) {
            System.err.println("API ERROR: Échec de la sérialisation de l'objet User.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur interne lors du traitement.")
                           .build();
        }
    }
}