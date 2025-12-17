package be.project.api;

import be.project.model.Gift;
import be.project.services.GiftService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.*; 
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

@Path("/gifts")
public class GiftAPI {
    
    private final GiftService giftService = new GiftService();
    private final ObjectMapper objectMapper; 

    public GiftAPI() {
        this.objectMapper = new ObjectMapper(); 
        this.objectMapper.registerModule(new JavaTimeModule()); 
        System.out.println("GIFTAPI: Constructeur appelé avec succès !");
    }

    // ===================================
    // 📖 GET : RÉCUPÉRATION DES CADEAUX
    // ===================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGiftsForUser(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            List<Gift> gifts = giftService.getAllGiftsForUser(userId);
            String jsonResponse = objectMapper.writeValueAsString(gifts);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================
    // 🎁 POST : CRÉATION (Utilise Gift + QueryParam pour wishlistId)
    // ===================================
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGift(Gift gift, @QueryParam("wishlistId") Integer wishlistId, @Context HttpHeaders headers) { 
        
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        if (gift == null || wishlistId == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Données manquantes ou wishlistId absent en paramètre\"}")
                            .build();
        }

        try {
            Gift createdGift = giftService.createGift(gift, wishlistId, userId);
            
            if (createdGift == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            return Response.status(Response.Status.CREATED)
                           .entity(objectMapper.writeValueAsString(createdGift))
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================
    // ✍️ PUT : MODIFICATION
    // ===================================
    @PUT
    @Path("/{id}") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyGift(@PathParam("id") int giftId, Gift gift, @QueryParam("wishlistId") Integer wishlistId, @Context HttpHeaders headers) {
        
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        if (gift == null || wishlistId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        gift.setId(giftId); // On s'assure que l'ID est celui de l'URL
        
        try {
            boolean success = giftService.updateGift(gift, wishlistId, userId);
            return success ? Response.status(Response.Status.NO_CONTENT).build() 
                           : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================
    // 🗑️ DELETE : SUPPRESSION
    // ===================================
    @DELETE
    @Path("/{id}") 
    public Response deleteGift(@PathParam("id") int giftId, @Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            boolean success = giftService.deleteGift(giftId, userId); 
            return success ? Response.status(Response.Status.NO_CONTENT).build() 
                           : Response.status(Response.Status.FORBIDDEN).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}