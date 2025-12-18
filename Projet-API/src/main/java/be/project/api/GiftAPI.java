package be.project.api;

import be.project.model.Gift;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.*; 
import javax.ws.rs.core.*;
import java.util.List;

@Path("/wishlists/{wishlistId}/gifts")
public class GiftAPI {
    
    private final ObjectMapper objectMapper; 

    public GiftAPI() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGiftsForUser(@Context HttpHeaders headers) {
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            // Appel à la méthode statique du modèle
            List<Gift> gifts = Gift.getAllForUser(userId);
            return Response.ok(objectMapper.writeValueAsString(gifts)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGift(@PathParam("wishlistId") int wishlistId, Gift gift, @Context HttpHeaders headers) { 
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            Gift created = gift.create(wishlistId, userId);
            if (created == null) return Response.status(Response.Status.FORBIDDEN).build();
            return Response.status(Response.Status.CREATED).entity(objectMapper.writeValueAsString(created)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyGift(@PathParam("wishlistId") int wishlistId, @PathParam("id") int giftId, Gift gift, @Context HttpHeaders headers) {
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        gift.setId(giftId); 
        try {
            boolean success = gift.update(wishlistId, userId);
            return success ? Response.noContent().build() : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}") 
    public Response deleteGift(@PathParam("id") int giftId, @Context HttpHeaders headers) {
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            Gift g = new Gift();
            g.setId(giftId);
            boolean success = g.delete(userId); 
            return success ? Response.noContent().build() : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}