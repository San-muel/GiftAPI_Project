package be.project.api;

import be.project.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

import javax.ws.rs.*; 
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserAPI {
    
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @POST 
    @Path("/login") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response login(User credentials) {
        if (credentials == null) return Response.status(Response.Status.BAD_REQUEST).build();
        
        try {
            User authenticatedUser = User.authenticate(credentials.getEmail(), credentials.getPsw()); 
            
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.ok(objectMapper.writeValueAsString(authenticatedUser)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(User newUser) {
        if (newUser == null) return Response.status(Response.Status.BAD_REQUEST).build();
        try {
            boolean isCreated = newUser.register();
            if (isCreated) {
                return Response.status(Response.Status.CREATED)
                               .entity("{\"message\": \"Utilisateur créé\"}")
                               .build();
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getUsers() {
        try {
            List<User> users = User.getAllUsers();
            return Response.ok(objectMapper.writeValueAsString(users)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") int id) {
        try {
            User user = User.findById(id); 
            
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            return Response.ok(objectMapper.writeValueAsString(user)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}