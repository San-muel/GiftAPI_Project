package be.project.api;

import be.project.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.*; 
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserAPI {
    
    private final ObjectMapper objectMapper; 

    public UserAPI() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON) 
    public Response login(@QueryParam("email") String email, @QueryParam("psw") String psw) {
        if (email == null || psw == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            // Appel à la méthode statique du modèle
            User authenticatedUser = User.authenticate(email, psw); 
            
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            
            return Response.ok(objectMapper.writeValueAsString(authenticatedUser)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(User newUser) { // Jackson désérialise automatiquement newUser
        if (newUser == null) return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            // Appel à la méthode d'instance du modèle
            boolean isCreated = newUser.register();

            if (isCreated) {
                return Response.status(Response.Status.CREATED)
                               .entity("{\"message\": \"Utilisateur créé\"}")
                               .build();
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}