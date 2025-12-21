package be.project.api;

import be.project.model.Wishlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.*; 
import javax.ws.rs.core.*;
import java.util.List;

@Path("/wishlists")
public class WishlistAPI {
    
    private final ObjectMapper objectMapper; 

    public WishlistAPI() {
        // Enregistrement du module JavaTimeModule pour gérer les LocalDate
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    // GET : Récupérer toutes les wishlists de l'utilisateur connecté
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWishlistsForUser(@Context HttpHeaders headers) {
        // Validation du token
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            List<Wishlist> wishlists = Wishlist.getAllForUser(userId);
            return Response.ok(objectMapper.writeValueAsString(wishlists)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST : Créer une nouvelle wishlist
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWishlist(Wishlist wishlist, @Context HttpHeaders headers) { 
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            // On appelle la méthode create du modèle en passant l'ID du user propriétaire
            Wishlist created = wishlist.create(userId);
            
            if (created == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            // On retourne 201 Created avec l'objet créé (qui contient le nouvel ID)
            return Response.status(Response.Status.CREATED).entity(objectMapper.writeValueAsString(created)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT : Modifier une wishlist existante
    @PUT
    @Path("/{id}") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyWishlist(@PathParam("id") int wishlistId, Wishlist wishlist, @Context HttpHeaders headers) {
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        // On force l'ID de l'objet avec celui de l'URL pour éviter les incohérences
        wishlist.setId(wishlistId); 
        
        try {
            boolean success = wishlist.update(userId);
            
            // Si success est false (ex: la liste n'appartient pas au user), on renvoie Forbidden
            return success ? Response.noContent().build() : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE : Supprimer une wishlist
    @DELETE
    @Path("/{id}") 
    public Response deleteWishlist(@PathParam("id") int wishlistId, @Context HttpHeaders headers) {
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            Wishlist w = new Wishlist();
            w.setId(wishlistId);
            
            boolean success = w.delete(userId); 
            
            return success ? Response.noContent().build() : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}