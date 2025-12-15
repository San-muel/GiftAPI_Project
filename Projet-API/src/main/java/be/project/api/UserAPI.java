package be.project.api;

import be.project.model.User;
import be.project.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import nécessaire
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
    
    // Instanciation et configuration de l'ObjectMapper
    private final ObjectMapper objectMapper; 

    public UserAPI() {
        // 1. Instanciation
        this.objectMapper = new ObjectMapper(); 
        
        // 2. Configuration CRUCIALE : Enregistrement du module Java 8 Date/Time
        this.objectMapper.registerModule(new JavaTimeModule()); 
        
        System.out.println("API CONFIG: ObjectMapper pour sérialisation manuelle configuré avec JavaTimeModule.");
    }

    /**
     * Endpoint pour l'authentification (login) avec mapping JSON manuel.
     * @param jsonPayload La chaîne JSON brute (String) envoyée par le client.
     * @return Response contenant le JSON de l'objet User complet + Token, ou un statut 401.
     */
    @POST
    @Path("/login") 
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
    public Response login(String jsonPayload) {
        
        String email = null;
        String psw = null;
        
        try {
            // --- ÉTAPE CRUCIALE 1 : DÉSÉRIALISATION MANUELLE (ENTRÉE) ---
            // Le Mapper configuré gère la désérialisation
            User inputUser = objectMapper.readValue(jsonPayload, User.class);
            email = inputUser.getEmail();
            psw = inputUser.getPsw();
            
        } catch (IOException e) {
            System.err.println("API ERROR: JSON d'entrée mal formé.");
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Format JSON invalide.")
                           .build();
        }
        
        System.out.println("API DEBUG: Tentative d'authentification pour : " + email);

        // 1. Appel à la couche Service
        User authenticatedUser = userService.authenticate(email, psw); 
        
        if (authenticatedUser == null) {
            // Échec
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"error\": \"Identifiants invalides\"}")
                           .build();
        }
        
        // --- ÉTAPE CRUCIALE 2 : SÉRIALISATION MANUELLE (SORTIE) ---
        try {
            // Le Mapper configuré gère la sérialisation de l'objet complet
            String jsonResponse = objectMapper.writeValueAsString(authenticatedUser);
            
            System.out.println("API DEBUG: Sérialisation manuelle réussie.");
            
            // 3. Retourne la réponse JSON sous forme de String
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
            
        } catch (IOException e) {
            // C'est cette erreur que nous voulons éviter maintenant
            System.err.println("API ERROR: Échec de la sérialisation de l'objet User.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erreur interne lors du traitement.")
                           .build();
        }
    }
}