package be.project.api;

import be.project.model.Wishlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.*; 
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

@Path("/wishlists")
public class WishlistAPI {
    
    private final ObjectMapper objectMapper; 

    public WishlistAPI() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); 
    }

    // ==========================================
    // GET : Récupérer MES listes (User connecté)
    // URL : /api/wishlists
    // ==========================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWishlists(
            @QueryParam("filter") String filter, 
            @Context HttpHeaders headers
    ) {
        try {
            // CAS 1 : Demande des listes publiques
            if ("public".equals(filter)) {
                System.out.println("[API] Demande de toutes les listes publiques (filter=public)");
                List<Wishlist> allLists = Wishlist.findAll();
                
                if (allLists == null || allLists.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                return Response.ok(objectMapper.writeValueAsString(allLists)).build();
            }

            // CAS 2 : Pas de filtre, donc on veut les listes de l'utilisateur (Token OBLIGATOIRE)
            int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
            
            if (userId == -1) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            System.out.println("[API] Demande des listes privées pour User ID : " + userId);
            List<Wishlist> userLists = Wishlist.getAllForUser(userId);
            return Response.ok(objectMapper.writeValueAsString(userLists)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("/all") 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        System.out.println("[DEBUG SERVEUR API] Réception demande publique (findAll)");

        try {
            // Appel direct au modèle sans passer par l'authentification
            List<Wishlist> wishlists = Wishlist.findAll(); 

            if (wishlists != null) {
                return Response.ok(objectMapper.writeValueAsString(wishlists)).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // POST : Créer une liste
    // URL : /api/wishlists
    // ==========================================
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWishlist(Wishlist wishlist, @Context HttpHeaders headers, @Context UriInfo uriInfo) { 
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (userId == -1) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            Wishlist created = wishlist.create(userId);
            
            if (created == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            // REST Best Practice : Retourner l'URI de la ressource créée dans le Header "Location"
            URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
            
            return Response.created(uri)
                           .entity(objectMapper.writeValueAsString(created))
                           .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // PUT : Modifier une liste
    // URL : /api/wishlists/{id}
    // ==========================================
    @PUT
    @Path("/{id}") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyWishlist(@PathParam("id") int wishlistId, Wishlist wishlist, @Context HttpHeaders headers) {
        // 1. Extraction et validation de l'utilisateur via le Token
        int userId = HelpMethode.validateAndExtractUserId(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
        
        System.out.println("======= [API PUT] RECEPTION MODIFICATION =======");
        System.out.println("[API] Wishlist ID à modifier : " + wishlistId);
        System.out.println("[API] User ID extrait du token : " + userId);
        
        if (userId == -1) {
            System.err.println("[API] Erreur : Token invalide ou utilisateur non autorisé.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 2. Préparation de l'objet métier
        wishlist.setId(wishlistId); 
        System.out.println("[API] Nouveau titre reçu : " + wishlist.getTitle());
        System.out.println("[API] Nouveau statut reçu : " + wishlist.getStatus());
        
        try {
            // 3. Appel de la couche métier/DAO (côté serveur)
            // C'est ici que la procédure Oracle PKG_WISHLIST_DATA.UPDATE_WISHLIST est appelée
            System.out.println("[API] Tentative de mise à jour en base de données...");
            boolean success = wishlist.update(userId);
            
            if (success) {
                System.out.println("[API] SUCCÈS : La base de données a été mise à jour.");
                return Response.noContent().build(); // 204
            } else {
                System.err.println("[API] ÉCHEC : La procédure SQL a retourné 0 (Propriétaire incorrect ou ID inexistant).");
                return Response.status(Response.Status.FORBIDDEN).build(); // 403
            }
        } catch (Exception e) {
            System.err.println("[API] ERREUR CRITIQUE : " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            System.out.println("======= [API PUT] FIN DE TRAITEMENT =======");
        }
    }

    // ==========================================
    // DELETE : Supprimer une liste
    // URL : /api/wishlists/{id}
    // ==========================================
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
	 // ==========================================
	 // GET : Récupérer une liste précise par son ID
	 // URL : /api/wishlists/{id}
	 // ==========================================
	 @GET
	 @Path("/{id}")
	 @Produces(MediaType.APPLICATION_JSON)
	 public Response getWishlistById(@PathParam("id") int id) {
	     System.out.println("[DEBUG SERVEUR API] Demande de détails pour la liste ID : " + id);
	
	     try {
	         // APPEL ACTIVE RECORD : Le modèle cherche en DB via le DAO
	         Wishlist wishlist = Wishlist.find(id);
	
	         if (wishlist != null) {
	             return Response.ok(objectMapper.writeValueAsString(wishlist)).build();
	         } else {
	             return Response.status(Response.Status.NOT_FOUND)
	                            .entity("{\"error\":\"Wishlist non trouvée\"}")
	                            .build();
	         }
	     } catch (Exception e) {
	         e.printStackTrace();
	         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	     }
	 }
}