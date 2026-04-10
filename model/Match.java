package model;

import exception.MatchInvalideException;
import java.time.LocalDateTime;

// Match — opposition entre deux equipes
public class Match {

  // compteur statique pour auto-incrementer l'id
  private static int compteur = 1;

  // statuts possibles d'un match
  public enum Statut { PLANIFIE, EN_COURS, TERMINE }

  // attributs
  private int           id;
  private Equipe        equipe1;
  private Equipe        equipe2;
  private int           scoreEquipe1;
  private int           scoreEquipe2;
  private Equipe        gagnant;
  private LocalDateTime dateHeure;
  private Statut        statut;
  private String        tournoiNom;

  // constructeur — lance une exception si les deux equipes sont identiques
  public Match(Equipe equipe1, Equipe equipe2, LocalDateTime dateHeure, String tournoiNom) {
    if (equipe1.getId() == equipe2.getId())
      throw new IllegalArgumentException("Une equipe ne peut pas jouer contre elle-meme.");

    this.id = compteur++;
    this.equipe1 = equipe1;
    this.equipe2 = equipe2;
    this.scoreEquipe1 = 0;
    this.scoreEquipe2 = 0;
    this.gagnant = null;
    this.dateHeure = dateHeure;
    this.statut = Statut.PLANIFIE;
    this.tournoiNom = tournoiNom != null ? tournoiNom : "";
  }

  // constructeur pour chargement depuis fichier
  public Match(int id, Equipe equipe1, Equipe equipe2, int score1, int score2,
               LocalDateTime dateHeure, Statut statut, String tournoiNom) {
    if (equipe1.getId() == equipe2.getId())
      throw new IllegalArgumentException("Une equipe ne peut pas jouer contre elle-meme.");

    this.id = id;
    this.equipe1 = equipe1;
    this.equipe2 = equipe2;
    this.scoreEquipe1 = score1;
    this.scoreEquipe2 = score2;
    this.gagnant = null;
    this.dateHeure = dateHeure;
    this.statut = statut;
    this.tournoiNom = tournoiNom != null ? tournoiNom : "";
    if(id >= compteur) compteur = id + 1;
  }

  // enregistre le resultat — valide les scores, determine le gagnant
  public void enregistrerResultat(int score1, int score2) throws MatchInvalideException {
    if (score1 < 0 || score2 < 0)
      throw new MatchInvalideException("Les scores ne peuvent pas etre negatifs.");

    this.scoreEquipe1 = score1;
    this.scoreEquipe2 = score2;
    this.statut = Statut.TERMINE;

    if (score1 > score2) {
      gagnant = equipe1;
      equipe1.enregistrerVictoire();
      equipe2.enregistrerDefaite();
    } else if (score2 > score1) {
      gagnant = equipe2;
      equipe2.enregistrerVictoire();
      equipe1.enregistrerDefaite();
    } else {
      gagnant = null; // egalite — aucun gagnant
    }
  }

  // indique si le match est termine
  public boolean estTermine() { return statut == Statut.TERMINE; }

  // indique si le match s'est termine sur une egalite
  public boolean estEgalite() { return estTermine() && gagnant == null; }

  // getters
  public int           getId()            { return id; }
  public Equipe        getEquipe1()       { return equipe1; }
  public Equipe        getEquipe2()       { return equipe2; }
  public int           getScoreEquipe1()  { return scoreEquipe1; }
  public int           getScoreEquipe2()  { return scoreEquipe2; }
  public Equipe        getGagnant()       { return gagnant; }
  public LocalDateTime getDateHeure()     { return dateHeure; }
  public Statut        getStatut()        { return statut; }
  public String        getTournoiNom()    { return tournoiNom; }

  // setters
  public void setEquipe1(Equipe equipe) {
    if (equipe == null) throw new IllegalArgumentException("L'equipe 1 ne peut pas etre null.");
    if (this.equipe2 != null && equipe.getId() == this.equipe2.getId()) {
      throw new IllegalArgumentException("Les deux equipes doivent etre differentes.");
    }
    this.equipe1 = equipe;
  }

  public void setEquipe2(Equipe equipe) {
    if (equipe == null) throw new IllegalArgumentException("L'equipe 2 ne peut pas etre null.");
    if (this.equipe1 != null && equipe.getId() == this.equipe1.getId()) {
      throw new IllegalArgumentException("Les deux equipes doivent etre differentes.");
    }
    this.equipe2 = equipe;
  }

  public void setScoreEquipe1(int score)   { this.scoreEquipe1 = Math.max(0, score); }
  public void setScoreEquipe2(int score)   { this.scoreEquipe2 = Math.max(0, score); }
  public void setDateHeure(LocalDateTime d){ this.dateHeure = d; }
  public void setGagnant(Equipe gagnant)  { this.gagnant = gagnant; }
  public void setStatut(Statut statut)    { this.statut = statut; }
  public void setTournoiNom(String nom)   { this.tournoiNom = nom; }

  @Override
  public String toString() {
    String res;
    if (estEgalite())        res = "Egalite";
    else if (gagnant != null) res = gagnant.getNom();
    else                      res = "N/A";
    return "[ID:" + id + "] " + equipe1.getNom() + " vs " + equipe2.getNom() +
           " | " + scoreEquipe1 + "-" + scoreEquipe2 +
           " | Gagnant: " + res +
           " | " + statut;
  }
}
