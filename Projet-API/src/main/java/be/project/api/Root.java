package be.project.api;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Root extends ResourceConfig {   // ← ResourceConfig, pas Application

    public Root() {
        // Enregistrement de tes ressources
        register(UserAPI.class);
        register(GiftAPI.class);
        register(HelpMethode.class); // si c'est un @Provider

        // ACTIVATION DU SUPPORT JSON JACKSON (c'est ÇA qui manquait !!)
        register(org.glassfish.jersey.jackson.JacksonFeature.class);

        System.out.println("ROOT: ResourceConfig initialisée – JacksonFeature activé !");
    }
}