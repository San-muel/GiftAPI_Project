package be.project.dto;

// Ce DTO (Data Transfer Object) est l'objet que l'API reçoit du client.
// Il doit contenir TOUS les champs envoyés par le client (Gift + wishlistId).
public class GiftCreationDTO {

    // Champs du modèle Gift
    // Pour simplifier l'utilisation de Jackson, nous utilisons des champs publics ou des getters/setters.
    public String name;
    public String description;
    public Double price;
    public Integer priority;
    public String photoUrl;

    // Champ additionnel (CRUCIAL) non présent dans l'objet Gift
    // C'est l'ID de la wishlist à laquelle le cadeau doit être ajouté.
    public Integer wishlistId; 
    
    // Constructeur sans argument nécessaire pour la désérialisation Jackson
    public GiftCreationDTO() {}

    // Optionnel : Vous pouvez ajouter des getters/setters si vous préférez des champs privés.

    // Getters et Setters (Exemple si les champs sont privés)
    /*
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // ...
    */
}