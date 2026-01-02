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
    // 1. Vérif de base du header
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        System.err.println("SECURITY ERROR: Header absent ou ne commence pas par Bearer");
        return -1;
    }

    // On retire "Bearer " pour garder le token
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    
    // CAS 1 : C'est le format complexe "fake-jwt-token-ID:..."
    if (token.startsWith("fake-jwt-token-")) {
        try {
            String payload = token.substring("fake-jwt-token-".length());
            String[] parts = payload.split(":");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            System.err.println("SECURITY ERROR: Échec parsing fake-jwt.");
        }
    }
    
    // CAS 2 (CELUI QUI TE SAUVE) : C'est juste l'ID brut (ex: "101")
    // C'est ici que ton code actuel va passer
    try {
        return Integer.parseInt(token);
    } catch (NumberFormatException e) {
        System.err.println("SECURITY ERROR: Le token '" + token + "' n'est ni un fake-jwt ni un ID numérique.");
        return -1;
    }
}
}