package model;

import exception.EsportException;
import exception.TournoiCompletException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Tournoi — competition entre equipes
public class Tournoi {

  // compteur statique pour auto-incrementer l'id
  private static int compteur = 1;

  // formats et statuts possibles
  public enum Format { ELIMINATION, ROUND_ROBIN, SUISSE }
  public enum Statut { EN_ATTENTE, EN_COURS, TERMINE }

  // attributs
  private int          id;
  private String       nom;
  private String       jeu;
  private Format       format;
  private LocalDate    dateDebut;
  private LocalDate    dateFin;
  private int          nbEquipesMax;
  private List<Equipe> equipes;
  private List<Match>  matchs;
  private Statut       statut;
  private Equipe       vainqueur;

  // constructeur
  public Tournoi(String nom, String jeu, Format format,
                 LocalDate dateDebut, LocalDate dateFin, int nbEquipesMax) {
    this.id = compteur++;
    this.nom = nom;
    this.jeu = jeu;
    this.format = format;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.nbEquipesMax = nbEquipesMax;
    this.equipes = new ArrayList<>();
    this.matchs = new ArrayList<>();
    this.statut = Statut.EN_ATTENTE;
  }

  // constructeur pour chargement depuis fichier
  public Tournoi(int id, String nom, String jeu, Format format,
                 LocalDate dateDebut, LocalDate dateFin, int nbEquipesMax, Statut statut) {
    this.id = id;
    this.nom = nom;
    this.jeu = jeu;
    this.format = format;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.nbEquipesMax = nbEquipesMax;
    this.equipes = new ArrayList<>();
    this.matchs = new ArrayList<>();
    this.statut = statut;
    if(id >= compteur) compteur = id + 1;
  }

  // inscrit une equipe — verifie le statut, la capacite et les doublons
  public void inscrireEquipe(Equipe equipe) throws EsportException {
    if (statut != Statut.EN_ATTENTE)
      throw new EsportException("Le tournoi " + nom + " n'est plus en phase d'inscription.");

    if (equipes.size() >= nbEquipesMax)
      throw new TournoiCompletException("Le tournoi " + nom + " est complet (" + nbEquipesMax + " equipes max).");

    for (Equipe e : equipes)
      if (e.getId() == equipe.getId())
        throw new EsportException("L'equipe " + equipe.getNom() + " est deja inscrite.");

    equipes.add(equipe);
  }

  // demarre le tournoi — necessite au moins 2 equipes
  public void demarrer() throws EsportException {
    if (statut != Statut.EN_ATTENTE)
      throw new EsportException("Le tournoi " + nom + " ne peut pas demarrer (statut: " + statut + ").");
    if (equipes.size() < 2)
      throw new EsportException("Il faut au moins 2 equipes pour demarrer le tournoi.");

    this.statut = Statut.EN_COURS;
  }

  // termine le tournoi en designant le vainqueur
  public void terminer(Equipe vainqueur) throws EsportException {
    if (statut != Statut.EN_COURS)
      throw new EsportException("Le tournoi " + nom + " n'est pas en cours.");

    this.statut = Statut.TERMINE;
    this.vainqueur = vainqueur;
  }

  // ajoute un match au tournoi
  public void ajouterMatch(Match match) {
    matchs.add(match);
  }

  // getters
  public int          getId()           { return id; }
  public String       getNom()          { return nom; }
  public String       getJeu()          { return jeu; }
  public Format       getFormat()       { return format; }
  public LocalDate    getDateDebut()    { return dateDebut; }
  public LocalDate    getDateFin()      { return dateFin; }
  public int          getNbEquipesMax() { return nbEquipesMax; }
  public List<Equipe> getEquipes()      { return equipes; }
  public List<Match>  getMatchs()       { return matchs; }
  public Statut       getStatut()       { return statut; }
  public Equipe       getVainqueur()    { return vainqueur; }

  // setters
  public void setNom(String nom)           { this.nom = nom; }
  public void setStatut(Statut statut)     { this.statut = statut; }
  public void setVainqueur(Equipe equipe)  { this.vainqueur = equipe; }

  @Override
  public String toString() {
    return "[ID:" + id + "] " + nom +
           " | " + jeu +
           " | Format: " + format +
           " | Equipes: " + equipes.size() + "/" + nbEquipesMax +
           " | Statut: " + statut;
  }
}
