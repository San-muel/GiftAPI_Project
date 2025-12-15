package be.project.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Configure l'ObjectMapper de Jackson pour gérer la sérialisation et la désérialisation
 * des types Java 8 Date/Time (LocalDate, LocalDateTime, etc.) dans l'API REST.
 */
@Provider // Indique à Jersey de découvrir et d'utiliser cette classe
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonObjectMapperProvider() {
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
}