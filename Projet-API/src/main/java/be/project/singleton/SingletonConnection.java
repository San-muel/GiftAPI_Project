package be.project.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnection {

    private static Connection connection;
    
    private static final String HOST = "193.190.64.10";
    private static final String PORT = "1522";
    private static final String SERVICE = "XEPDB1";
    private static final String USER = "STUDENT03_12";
    private static final String PASS = "changeme";
    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE;

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver Oracle introuvable !");
        }
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("DEBUG: Ouverture d'une nouvelle connexion Oracle...");
                connection = DriverManager.getConnection(URL, USER, PASS);
                connection.setAutoCommit(false); 
            }
        } catch (SQLException e) {
            System.err.println("CRITICAL ERROR: Impossible de reconnecter Oracle.");
            e.printStackTrace();
        }
        return connection;
    }
}