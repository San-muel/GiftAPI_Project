package be.project.api;

import be.project.model.Gift;
import be.project.services.GiftService;
import be.project.dto.GiftCreationDTO;
import be.project.dto.GiftUpdateDTO; 
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
        System.out.println("GIFTAPI: Constructeur appelé avec succès ! ObjectMapper configuré.");
    }

    // ===================================
    // 📖 GET : RÉCUPÉRATION DE TOUS LES CADEAUX DE L'UTILISATEUR
    // ===================================
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGiftsForUser(@Context HttpHeaders headers) {
        
        System.out.println("GIFTAPI: Méthode GET /gifts atteinte.");

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        if (userId == -1) {
            System.err.println("GIFTAPI ERROR (GET): Accès non autorisé.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            List<Gift> gifts = giftService.getAllGiftsForUser(userId);
            
            String jsonResponse = objectMapper.writeValueAsString(gifts);
            System.out.println("GIFTAPI DEBUG (GET): Récupération réussie. Nombre de cadeaux: " + gifts.size());
            return Response.ok(jsonResponse).build(); // 200 OK
            
        } catch (Exception e) {
            System.err.println("GIFTAPI ERROR (GET): Erreur interne lors de l'appel service.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    // ===================================
    // 🎁 POST : CRÉATION DE CADEAU (Chemin: /gifts)
    // ===================================
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGift(GiftCreationDTO giftDto, @Context HttpHeaders headers) { 
        
        System.out.println("GIFTAPI: Méthode POST /gifts (Création) atteinte.");

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        if (userId == -1) {
            System.err.println("GIFTAPI ERROR (POST): Accès non autorisé.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (giftDto == null || giftDto.wishlistId == null) {
             System.err.println("GIFTAPI ERROR (POST): DTO nul ou manque wishlistId.");
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Format JSON invalide ou wishlistId manquant\"}")
                            .build();
        }

        Gift giftToCreate = new Gift();
        giftToCreate.setName(giftDto.name);
        giftToCreate.setDescription(giftDto.description);
        giftToCreate.setPrice(giftDto.price);
        giftToCreate.setPriority(giftDto.priority);
        giftToCreate.setPhotoUrl(giftDto.photoUrl);

        try {
            Gift createdGift = giftService.createGift(giftToCreate, giftDto.wishlistId, userId);
            
            if (createdGift == null) {
                return Response.status(Response.Status.FORBIDDEN) 
                               .entity("{\"error\": \"Non autorisé à ajouter un cadeau à cette wishlist ou wishlist introuvable\"}")
                               .build();
            }

            String jsonResponse = objectMapper.writeValueAsString(createdGift);
            System.out.println("GIFTAPI DEBUG (POST): Création réussie. Renvoi 201.");
            return Response.status(Response.Status.CREATED).entity(jsonResponse).build();
            
        } catch (Exception e) {
            System.err.println("GIFTAPI ERROR (POST): Erreur interne lors de l'appel service.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    // ===================================
    // ✍️ PUT : MODIFICATION DE CADEAU (Chemin: /gifts/{id})
    // ===================================
    
    @PUT
    @Path("/{id}") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyGift(@PathParam("id") int giftId, GiftUpdateDTO giftDto, @Context HttpHeaders headers) {
        
        System.out.println("GIFTAPI: Méthode PUT /gifts/" + giftId + " (Modification) atteinte.");

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        if (userId == -1) {
            System.err.println("GIFTAPI ERROR (PUT): Accès non autorisé.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 2. Validation DTO & Cohérence des IDs
        if (giftDto == null || giftDto.id == null || giftDto.id != giftId || giftDto.wishlistId == null) {
            System.err.println("GIFTAPI ERROR (PUT): Incohérence des IDs (URL vs Body) ou wishlistId/DTO manquant.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"ID cadeau manquant ou incohérent, ou wishlistId manquant.\"}")
                           .build();
        }

        // 3. Conversion DTO → Model Gift
        Gift giftToUpdate = new Gift();
        giftToUpdate.setId(giftId); 
        giftToUpdate.setName(giftDto.name);
        giftToUpdate.setDescription(giftDto.description);
        giftToUpdate.setPrice(giftDto.price);
        giftToUpdate.setPriority(giftDto.priority);
        giftToUpdate.setPhotoUrl(giftDto.photoUrl);
        
        try {
            boolean success = giftService.updateGift(giftToUpdate, giftDto.wishlistId, userId);
            
            if (success) {
                System.out.println("GIFTAPI DEBUG (PUT): Mise à jour réussie pour ID: " + giftId);
                return Response.status(Response.Status.NO_CONTENT).build(); // 204 No Content
            } else {
                return Response.status(Response.Status.FORBIDDEN) 
                               .entity("{\"error\": \"Non autorisé à modifier ce cadeau, ou cadeau/wishlist introuvable.\"}")
                               .build();
            }
        } catch (Exception e) {
            System.err.println("GIFTAPI ERROR (PUT): Erreur interne lors de l'appel service.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================
    // 🗑️ DELETE : SUPPRESSION DE CADEAU (Chemin: /gifts/{id})
    // ===================================
    
    @DELETE
    @Path("/{id}") 
    public Response deleteGift(@PathParam("id") int giftId, @Context HttpHeaders headers) {
        
        System.out.println("GIFTAPI: Méthode DELETE /gifts/" + giftId + " (Suppression) atteinte.");

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        int userId = HelpMethode.validateAndExtractUserId(authHeader);
        if (userId == -1) {
            System.err.println("GIFTAPI ERROR (DELETE): Accès non autorisé.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        // 2. Validation de l'ID
        if (giftId <= 0) {
            System.err.println("GIFTAPI ERROR (DELETE): ID cadeau manquant ou invalide.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"ID cadeau manquant ou invalide.\"}")
                           .build();
        }

        try {
            // Le service appelle le DAO qui appelle la procédure stockée (vérification d'autorisation incluse)
            boolean success = giftService.deleteGift(giftId, userId); 
            
            if (success) {
                System.out.println("GIFTAPI DEBUG (DELETE): Suppression réussie pour ID: " + giftId);
                return Response.status(Response.Status.NO_CONTENT).build(); // 204 No Content
            } else {
                // Si la suppression échoue, c'est généralement car l'utilisateur n'est pas autorisé 
                // ou que la ressource n'existe plus.
                return Response.status(Response.Status.FORBIDDEN) 
                               .entity("{\"error\": \"Non autorisé à supprimer ce cadeau, ou cadeau introuvable.\"}")
                               .build();
            }
        } catch (Exception e) {
            System.err.println("GIFTAPI ERROR (DELETE): Erreur interne lors de l'appel service.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}