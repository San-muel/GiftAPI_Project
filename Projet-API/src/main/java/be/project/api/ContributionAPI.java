package be.project.api;

import be.project.model.Contribution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/contributions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContributionAPI {

    private final ObjectMapper objectMapper;

    public ContributionAPI() {
        // --- CONFIGURATION JACKSON POUR CORRIGER LES DATES ---
        this.objectMapper = new ObjectMapper();
        // Permet de gérer les LocalDateTime (Java 8+)
        this.objectMapper.registerModule(new JavaTimeModule());
        // INDISPENSABLE : Empêche la conversion des dates en tableau [2025, 12, 25...]
        // Les force en format String "2025-12-25T..."
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GET
    @Path("/{id}")
    public Response find(@PathParam("id") int id) {
        System.out.println("API: Recherche contribution ID " + id);
        Contribution c = Contribution.find(id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        try {
            // On utilise notre objectMapper configuré pour envoyer le JSON
            return Response.ok(objectMapper.writeValueAsString(c)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    public Response findAll() {
        List<Contribution> contributions = Contribution.findAll();

        if (contributions.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        try {
            return Response.ok(objectMapper.writeValueAsString(contributions)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/gift/{giftId}")
    public Response findAllByGift(@PathParam("giftId") int giftId) {
        System.out.println("API: Demande contributions pour cadeau ID: " + giftId);

        List<Contribution> list = Contribution.findAllByGiftId(giftId);

        if (list == null || list.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        try {
            return Response.ok(objectMapper.writeValueAsString(list)).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @POST
    public Response create(Contribution contribution) {
        // --- LOGS DE DEBUG ---
        System.out.println("API POST: Réception demande création contribution");
        System.out.println(" -> Amount: " + contribution.getAmount());
        System.out.println(" -> Comment: " + contribution.getComment());
        System.out.println(" -> GiftID: " + contribution.getGiftId());
        System.out.println(" -> UserID: " + contribution.getUserId());
        
        // Vérification des données critiques
        if (contribution.getGiftId() == 0 || contribution.getUserId() == 0) {
            System.err.println("API ERREUR: GiftId ou UserId est 0 ! Vérifiez le JSON envoyé.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("GiftId ou UserId manquant").build();
        }

        boolean success = contribution.save();
        
        if (success) {
            System.out.println("API: Sauvegarde réussie !");
            try {
                return Response.status(Response.Status.CREATED)
                        .entity(objectMapper.writeValueAsString(contribution))
                        .build();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
        } else {
            System.err.println("API: Echec de la sauvegarde (DAO a retourné false)");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}