package be.project.api;

import be.project.model.SharedWishlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.*; 
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/shared-wishlists")
public class SharedWishlistAPI {
    
    private final ObjectMapper objectMapper; 

    public SharedWishlistAPI() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    @POST
    @Path("/") // On précise que c'est la racine du Path défini sur la classe ("/shared-wishlists")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response share(Map<String, Object> data) {
	    // --- PRINT DE DEBUG ---
	    System.out.println("[DEBUG SERVEUR API] Route /shared-wishlists contactée !");
	    
	    try {
	        System.out.println("[DEBUG SERVEUR API] Payload reçu : " + data);
	
	        int wishlistId = Integer.parseInt(data.get("wishlistId").toString());
	        int targetUserId = Integer.parseInt(data.get("targetUserId").toString());
	        String notification = (String) data.get("notification");
	
	        System.out.println("[DEBUG SERVEUR API] Traitement : Wishlist=" + wishlistId + " -> User=" + targetUserId);
	
	        boolean success = SharedWishlist.createLink(wishlistId, targetUserId, notification);
	
	        System.out.println("[DEBUG SERVEUR API] Succès en base de données ? " + success);
	
	        if (success) {
	            return Response.status(Response.Status.CREATED).build();
	        } else {
	            return Response.status(Response.Status.CONFLICT)
	                           .entity("{\"error\": \"Échec du partage\"}")
	                           .build();
	        }
	    } catch (Exception e) {
	        System.out.println("[DEBUG SERVEUR API] ERREUR : " + e.getMessage());
	        e.printStackTrace();
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}
}