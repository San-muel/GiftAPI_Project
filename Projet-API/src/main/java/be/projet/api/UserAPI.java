package be.projet.api;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.projet.DAO.UserDao;
import be.projet.model.User;

@Path("/user")
public class UserAPI {

    // 1. Récupérer tous les utilisateurs
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        List<User> users = UserDao.getInstance().getAllUsers();
        return Response.status(Response.Status.OK).entity(users).build();
    }

    // 2. Récupérer un utilisateur par ID
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getUser(@PathParam("id") int id) {
        // Attention: Il faudra ajouter cette méthode dans votre UserDao
        User user = UserDao.getInstance().getUserById(id);
        
        if (user != null) {
            return Response.status(Response.Status.OK).entity(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // 3. Ajouter un utilisateur
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(String userJSON) {
        try {
            // Conversion du JSON reçu en objet Java User
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(userJSON, User.class);

            // Validation simple
            if(user.getNom() == null || user.getPrenom() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                       .entity("Nom et Prénom sont requis.")
                       .build();
            }

            // Appel au DAO pour créer l'user en DB
            // Attention: Il faudra ajouter cette méthode dans votre UserDao
            int newId = UserDao.getInstance().createUser(user);

            // Mise à jour de l'ID de l'objet pour le retour
            user.setId(newId);

            return Response.status(Response.Status.CREATED)
                           .entity(user) // On renvoie l'objet créé avec son ID
                           .header("Location", "user/" + newId)
                           .build();

        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Invalid JSON data: " + e.getMessage())
                           .build();
        }
    }
}