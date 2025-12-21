package be.project.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature; // Import explicite c'est mieux
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Root extends ResourceConfig { 

    public Root() {
        // --- TES RESSOURCES ---
        register(UserAPI.class);
        register(GiftAPI.class);

        register(WishlistAPI.class); 

        register(HelpMethode.class); 

        // --- CONFIGURATION ---
        // Activation du support JSON
        register(JacksonFeature.class);

        System.out.println("ROOT: ResourceConfig initialisée. WishlistAPI est enregistrée !");
    }
}