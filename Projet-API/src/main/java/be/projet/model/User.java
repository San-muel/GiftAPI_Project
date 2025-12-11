package be.projet.model;

public class User {
    private int id;
    private String nom;
    private String prenom;
    // Ajoutez d'autres champs selon vos colonnes (email, password, etc.)

    public User() {}

    public User(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters et Setters (Indispensables pour la conversion JSON automatique)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}