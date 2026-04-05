package model;

import java.time.LocalDate;

// Joueur herite de Utilisateur
public class Joueur extends Utilisateur {

  // attributs specifiques au joueur
  private String jeuPrincipal;
  private String rang; // Bronze / Silver / Gold / Platinum / Diamond
  private int scoreTotal;
  private String nomEquipe;

  // constructeur
  public Joueur(String pseudo, String email, String niveau, String jeuPrincipal) {
    super(pseudo, email, niveau);
    this.jeuPrincipal = jeuPrincipal;
    this.rang = "Bronze";
    this.scoreTotal = 0;
    this.nomEquipe = "";
  }

  // constructeur pour chargement depuis fichier
  public Joueur(int id, String pseudo, String email, String niveau, LocalDate dateIns,
                String jeuPrincipal, String rang, int scoreTotal, String nomEquipe) {
    super(id, pseudo, email, niveau, dateIns);
    this.jeuPrincipal = jeuPrincipal;
    this.rang = rang;
    this.scoreTotal = scoreTotal;
    this.nomEquipe = nomEquipe;
  }

  // ajoute des points et recalcule le rang automatiquement
  public void ajouterPoints(int points) {
    if (points > 0) scoreTotal += points;
    mettreAJourRang();
  }

  // recalcule le rang selon le score total
  public void mettreAJourRang() {
    if      (scoreTotal >= 10000) rang = "Diamond";
    else if (scoreTotal >= 5000)  rang = "Platinum";
    else if (scoreTotal >= 2000)  rang = "Gold";
    else if (scoreTotal >= 500)   rang = "Silver";
    else                          rang = "Bronze";
  }

  // methode abstraite implementee — presentation du joueur
  @Override
  public String sePresenter() {
    return "Joueur: " + getPseudo() +
           " | Jeu: " + jeuPrincipal +
           " | Rang: " + rang +
           " | Score: " + scoreTotal +
           " | Equipe: " + (nomEquipe.isEmpty() ? "Aucune" : nomEquipe);
  }

  // retourne le role de cet utilisateur
  @Override
  public String getRole() { return "Joueur"; }

  // getters
  public String getJeuPrincipal() { return jeuPrincipal; }
  public String getRang()         { return rang; }
  public int    getScoreTotal()   { return scoreTotal; }
  public String getNomEquipe()    { return nomEquipe; }

  // setters
  public void setJeuPrincipal(String jeuPrincipal) { this.jeuPrincipal = jeuPrincipal; }
  public void setRang(String rang)                 { this.rang = rang; }
  public void setScoreTotal(int scoreTotal)        { this.scoreTotal = scoreTotal; }
  public void setNomEquipe(String nomEquipe)       { this.nomEquipe = nomEquipe; }

  @Override
  public String toString() {
    return "[ID:" + getId() + "] " + getPseudo() +
           " | " + jeuPrincipal + " | " + rang + " | " + scoreTotal + " pts";
  }
}
