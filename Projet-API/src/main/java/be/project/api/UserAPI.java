package be.project.api;

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

// L'importation de UserDAO n'est plus nécessaire car on appelle le Modèle.
// import be.project.DAO.UserDAO;
import be.project.model.User;

@Path("/user")
public class UserAPI {

}