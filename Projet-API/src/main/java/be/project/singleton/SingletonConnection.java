package be.project.singleton; // Adaptez le package selon votre structure

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnection {

    // 1. La variable statique qui contient l'unique instance de la connexion
    private static Connection connection;

    // 2. Le bloc static s'exécute une seule fois au chargement de la classe
    static {
        try {
            // A. Chargement du Driver Oracle
            // Pour ojdbc11 (moderne) ou ojdbc6
            Class.forName("oracle.jdbc.OracleDriver");

            // B. Paramètres récupérés de votre image
            String host = "193.190.64.10";
            String port = "1522";           // Attention, ce n'est pas le standard 1521
            String service = "XEPDB1";      // Nom de service (et non SID)
            String user = "STUDENT03_12";
            String password = "changeme";

            // C. Construction de l'URL JDBC
            // Syntaxe pour "Nom de Service" : jdbc:oracle:thin:@//host:port/service
            String url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service;

            // D. Création de la connexion
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion à Oracle réussie !");

        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver Oracle introuvable. Vérifiez le pom.xml.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Impossible de se connecter à la base.");
            e.printStackTrace();
        }
    }

    // 3. Méthode pour récupérer la connexion depuis n'importe où dans le projet
    public static Connection getConnection() {
        return connection;
    }
}