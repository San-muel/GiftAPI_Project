package be.project.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature; 
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Root extends ResourceConfig { 

    public Root() {
        // --- ENREGISTREMENT EXPLICITE DES RESSOURCES ---
        // C'est plus sûr que le scan automatique pour éviter les erreurs 404
        register(UserAPI.class);
        register(GiftAPI.class);
        register(WishlistAPI.class); // Indispensable pour que la création de liste fonctionne
        register(HelpMethode.class); 

        // --- CONFIGURATION ---
        // Activation du support JSON pour Jackson
        register(JacksonFeature.class);

        System.out.println("ROOT: ResourceConfig initialisée. WishlistAPI est enregistrée !");
    }
}