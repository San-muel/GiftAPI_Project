package be.project.api;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Root extends ResourceConfig {

    public Root() {
        packages("be.project.api"); 
        register(org.glassfish.jersey.jackson.JacksonFeature.class);

        System.out.println("ROOT: ResourceConfig initialisée avec SCAN AUTOMATIQUE !");
    }
}