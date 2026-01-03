package be.project.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class HelpMethode implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public HelpMethode() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        System.out.println("API CONFIG: JavaTimeModule de Jackson enregistré avec succès.");
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    public static int validateAndExtractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.err.println("SECURITY ERROR: Header absent ou ne commence pas par Bearer");
            return -1;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        
        if (token.startsWith("fake-jwt-token-")) {
            try {
                String payload = token.substring("fake-jwt-token-".length());
                String[] parts = payload.split(":");
                return Integer.parseInt(parts[0]);
            } catch (Exception e) {
                System.err.println("SECURITY ERROR: Échec parsing fake-jwt.");
            }
        }
        
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            System.err.println("SECURITY ERROR: Le token '" + token + "' n'est ni un fake-jwt ni un ID numérique.");
            return -1;
        }
    }
}