package be.project.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Configure l'ObjectMapper de Jackson pour gérer la sérialisation et la désérialisation
 * des types Java 8 Date/Time (LocalDate, LocalDateTime, etc.) dans l'API REST.
 */
@Provider // Indique à Jersey de découvrir et d'utiliser cette classe
public class HelpMethode implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public HelpMethode() {
        mapper = new ObjectMapper();
        
        // C'EST LA LIGNE CRUCIALE : Enregistrement du module Java Time
        mapper.registerModule(new JavaTimeModule());
        
        System.out.println("API CONFIG: JavaTimeModule de Jackson enregistré avec succès.");

        // OPTIONNEL : Désactiver l'écriture des dates comme timestamps (facilite la lecture JSON)
        // mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); 
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // Pour un JSON lisible
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
    /**
     * Valide le format du token et extrait l'ID utilisateur.
     * ATTENTION : Ceci est une implémentation simplifiée pour le token de démonstration "fake-jwt-token-ID:email:timestamp".
     * Dans un vrai projet, cela nécessiterait une librairie JWT (ex: JJWT).
     *
     * @param authorizationHeader L'en-tête "Authorization: Bearer <token>"
     * @return L'ID utilisateur (int) s'il est valide, sinon -1.
     */
    public static int validateAndExtractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return -1;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        
        // Validation du format du token factice : "fake-jwt-token-ID:email:timestamp"
        if (token.startsWith("fake-jwt-token-")) {
            String payload = token.substring("fake-jwt-token-".length());
            String[] parts = payload.split(":");
            
            if (parts.length >= 1) {
                try {
                    // La première partie est censée être l'ID utilisateur
                    return Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    System.err.println("SECURITY ERROR: ID utilisateur non numérique dans le payload du token.");
                }
            }
        }
        
        System.err.println("SECURITY ERROR: Token invalide ou malformé.");
        return -1;
    }
}