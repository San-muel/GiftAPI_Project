package be.project.api;

// import be.project.DAO.UserDAO; // Non nécessaire ici
import be.project.model.Contribution;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List; // Ajout de l'import pour List

@Path("/contributions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContributionAPI {

	@GET
    @Path("/{id}")
    public Response find(@PathParam("id") int id){
    		System.out.println("je suis dans l'api ContributionService - find(" + id + ")");
        // L'API appelle le modèle directement (déjà correct)
        Contribution c = Contribution.find(id); 

        if(c == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(c).build();
    }

    @GET
    public Response findAll(){
        // CORRECTION : L'API appelle la méthode statique du Modèle (Contribution)
        List<Contribution> contributions = Contribution.findAll();
        
        if (contributions.isEmpty()) {
             return Response.status(Response.Status.NO_CONTENT).build(); // 204 No Content
        }
        
        return Response.ok(contributions).build();
    }
}