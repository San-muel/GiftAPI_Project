package be.project.dto;

public class GiftUpdateDTO {
    public Integer id; // ID du cadeau à modifier
    public String name;
    public String description;
    public Double price;
    public Integer priority;
    public String photoUrl;
    public Integer wishlistId; // ID de la liste parente (pour validation/sécurité)
    
    // Constructeur par défaut nécessaire pour Jackson
    public GiftUpdateDTO() {}
}