package be.projet.model;

import be.projet.DAO.SingletonConnection;

public class Test {
    public static void main(String[] args) {
        // Appeler getConnection va déclencher le bloc static et tenter la connexion
        java.sql.Connection c = SingletonConnection.getConnection();
        
        if (c != null) {
            System.out.println("Le test est validé !");
        }else {
            System.out.println("marche pas !");
        }
    }
}