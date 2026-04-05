package model;
import java.time.LocalDate;

// abstract = ne peut pas etre instanciee directement
public abstract class Utilisateur {

  // attributs prives — encapsulation stricte
  private int id;
  private String pseudo;
  private String email;
  private String niveau;
  private LocalDate dateInscription;
  // compteur statique pour auto-incrementer l'id
  private static int compteur = 1;

  // constructeur
  public Utilisateur(String pseudo, String email, String niveau) {
    this.id = compteur++;
    this.pseudo = pseudo;
    this.email = email;
    this.niveau = niveau;
    this.dateInscription = LocalDate.now();
  }

  // constructeur pour chargement depuis fichier
  public Utilisateur(int id, String pseudo, String email, String niveau, LocalDate dateInscription) {
    this.id = id;
    this.pseudo = pseudo;
    this.email = email;
    this.niveau = niveau;
    this.dateInscription = dateInscription;
    if(id >= compteur) compteur = id + 1;
  }

  // methode ABSTRAITE — chaque sous-classe l'implemente a sa facon
  public abstract String sePresenter();

  // methode concrete — partagee par tous
  public String getRole() { return "Utilisateur"; }

  // getters
  public int getId() { return id; }
  public String getPseudo() { return pseudo; }
  public String getEmail() { return email; }
  public String getNiveau() { return niveau; }
  public LocalDate getDateInscription() { return dateInscription; }

  // setters avec validation
  public void setPseudo(String pseudo) {
    if (pseudo != null && !pseudo.trim().isEmpty())
      this.pseudo = pseudo;
  }
  public void setEmail(String email) {
    if (email != null && email.contains("@"))
      this.email = email;
  }
  public void setNiveau(String niveau) { this.niveau = niveau; }

  // toString — base pour toutes les sous-classes
  @Override
  public String toString() {
    return "[ID:" + id + "] " + pseudo +
           " | " + email + " | Niveau: " + niveau;
  }
}