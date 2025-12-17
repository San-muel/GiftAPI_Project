package be.project.api;

import be.project.model.User;
import be.project.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam; // IMPORTANT : à ajouter
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserAPI {
    
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper; 

    public UserAPI() {
        this.objectMapper = new ObjectMapper(); 
        this.objectMapper.registerModule(new JavaTimeModule()); 
        System.out.println("API CONFIG: ObjectMapper configuré avec JavaTimeModule.");
    }

    /**
     * AUTHENTIFICATION (Login) : GET /users?email=...&psw=...
     * On récupère les identifiants via @QueryParam car c'est un GET (pas de body).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON) 
    public Response login(@QueryParam("email") String email, @QueryParam("psw") String psw) {
        
        System.out.println("API DEBUG: Tentative de GET (login) pour : " + email);

        if (email == null || psw == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Paramètres email ou psw manquants dans l'URL\"}")
                           .build();
        }

        // 1. Appel à la couche Service
        User authenticatedUser = userService.authenticate(email, psw); 
        
        if (authenticatedUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("{\"error\": \"Identifiants invalides\"}")
                           .build();
        }
        
        // 2. Sérialisation de l'utilisateur trouvé pour la réponse
        try {
            String jsonResponse = objectMapper.writeValueAsString(authenticatedUser);
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            System.err.println("API ERROR: Échec de la sérialisation.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * INSCRIPTION (Register) : POST /users
     * Ici on garde le jsonPayload car le POST contient un corps (le nouvel utilisateur).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String jsonPayload) {
        try {
            // Désérialisation du JSON reçu pour créer l'objet User
            User newUser = objectMapper.readValue(jsonPayload, User.class);
            
            System.out.println("API DEBUG: Tentative de POST (inscription) pour : " + newUser.getEmail());

            boolean isCreated = userService.register(newUser);

            if (isCreated) {
                return Response.status(Response.Status.CREATED)
                               .entity("{\"message\": \"Utilisateur créé avec succès\"}")
                               .build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                               .entity("{\"error\": \"L'utilisateur existe déjà\"}")
                               .build();
            }

        } catch (IOException e) {
            System.err.println("API ERROR: JSON d'inscription mal formé.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Format JSON invalide\"}")
                           .build();
        }
    }
}