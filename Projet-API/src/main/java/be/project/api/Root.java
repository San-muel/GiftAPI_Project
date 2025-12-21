package be.project.api;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Root extends ResourceConfig {

    public Root() {
        // --- SCAN AUTOMATIQUE ---
        // Jersey va scanner récursivement ce package pour trouver 
        // toutes les classes annotées avec @Path
        packages("be.project.api"); 

        // ACTIVATION DU SUPPORT JSON JACKSON
        register(org.glassfish.jersey.jackson.JacksonFeature.class);

        System.out.println("ROOT: ResourceConfig initialisée avec SCAN AUTOMATIQUE !");
    }
}