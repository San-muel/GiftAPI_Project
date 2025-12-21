package be.project.api;

import be.project.model.Wishlist;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/wishlists")
public class WishlistAPI {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        System.out.println("[DEBUG SERVEUR API] Réception d'une demande de liste de wishlists");
        
        try {
            // Appel Active Record : Le modèle gère lui-même sa récupération
            List<Wishlist> wishlists = Wishlist.findAll();

            if (wishlists != null) {
                System.out.println("[DEBUG SERVEUR API] Nombre de listes trouvées : " + wishlists.size());
                return Response.ok(wishlists).build(); // Envoie le code 200 avec la liste
            } else {
                return Response.status(Response.Status.NO_CONTENT).build(); // 204 si vide
            }
        } catch (Exception e) {
            System.err.println("[DEBUG SERVEUR API] Erreur lors du findAll : " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}