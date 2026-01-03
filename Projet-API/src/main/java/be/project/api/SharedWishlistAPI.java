package be.project.api;

import be.project.model.SharedWishlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.*; 
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import javax.ws.rs.core.*;

@Path("/shared-wishlists")
public class SharedWishlistAPI {
    
    private final ObjectMapper objectMapper; 

    public SharedWishlistAPI() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response share(Map<String, Object> data, @Context HttpHeaders headers) {
        
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            int wishlistId = Integer.parseInt(String.valueOf(data.get("wishlistId")));
            int targetUserId = Integer.parseInt(String.valueOf(data.get("targetUserId")));
            String notification = (String) data.get("notification");

            boolean success = SharedWishlist.createLink(wishlistId, targetUserId, notification);

            if (success) {
                return Response.status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                               .entity("{\"error\": \"Déjà partagé ou erreur\"}")
                               .build();
            }
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Données invalides").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}