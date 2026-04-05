package model;

import exception.EsportException;
import exception.JoueurDejaInscritException;
import java.util.ArrayList;
import java.util.List;

// Equipe — groupe de joueurs avec un coach et un capitaine
public class Equipe {

  // nombre maximum de joueurs par equipe
  public static final int MAX_JOUEURS = 5;

  // compteur statique pour auto-incrementer l'id
  private static int compteur = 1;

  // attributs
  private int          id;
  private String       nom;
  private String       jeu;
  private List<Joueur> joueurs;
  private Coach        coach;
  private Joueur       capitaine;
  private int          nbVictoires;
  private int          nbDefaites;

  // constructeur
  public Equipe(String nom, String jeu) {
    this.id = compteur++;
    this.nom = nom;
    this.jeu = jeu;
    this.joueurs = new ArrayList<>();
    this.nbVictoires = 0;
    this.nbDefaites = 0;
  }

  // constructeur pour chargement depuis fichier
  public Equipe(int id, String nom, String jeu, int nbVictoires, int nbDefaites) {
    this.id = id;
    this.nom = nom;
    this.jeu = jeu;
    this.joueurs = new ArrayList<>();
    this.nbVictoires = nbVictoires;
    this.nbDefaites = nbDefaites;
    if(id >= compteur) compteur = id + 1;
  }

  // ajoute un joueur — verifie que l'equipe n'est pas pleine ni en doublon
  public void ajouterJoueur(Joueur joueur) throws EsportException {
    if (joueurs.size() >= MAX_JOUEURS)
      throw new EsportException("L'equipe " + nom + " est pleine (" + MAX_JOUEURS + " joueurs max).");

    for (Joueur j : joueurs)
      if (j.getId() == joueur.getId())
        throw new JoueurDejaInscritException("Le joueur " + joueur.getPseudo() + " est deja dans l'equipe.");

    joueurs.add(joueur);
    joueur.setNomEquipe(nom); // mise a jour cote joueur
  }

  // retire un joueur de l'equipe
  public void retirerJoueur(Joueur joueur) throws EsportException {
    boolean supprime = joueurs.removeIf(j -> j.getId() == joueur.getId());
    if (!supprime)
      throw new EsportException("Le joueur " + joueur.getPseudo() + " n'est pas dans l'equipe.");

    joueur.setNomEquipe("");
    // si c'etait le capitaine, on le supprime
    if (capitaine != null && capitaine.getId() == joueur.getId())
      capitaine = null;
  }

  // definit le capitaine — le joueur doit deja etre dans l'equipe
  public void definirCapitaine(Joueur joueur) throws EsportException {
    boolean membre = false;
    for (Joueur j : joueurs)
      if (j.getId() == joueur.getId()) { membre = true; break; }

    if (!membre)
      throw new EsportException("Le joueur " + joueur.getPseudo() + " doit etre membre de l'equipe.");

    this.capitaine = joueur;
  }

  // affecte un coach — libere l'ancien coach s'il y en avait un
  public void affecterCoach(Coach coach) {
    if (this.coach != null) this.coach.setEquipeCoachee("");
    this.coach = coach;
    if (coach != null) coach.setEquipeCoachee(nom);
  }

  // enregistre une victoire (equipe + coach)
  public void enregistrerVictoire() {
    nbVictoires++;
    if (coach != null) coach.enregistrerVictoire();
  }

  // enregistre une defaite
  public void enregistrerDefaite() {
    nbDefaites++;
  }

  // ratio victoires / matchs joues (0.0 si aucun match)
  public double getRatioVictoires() {
    int total = nbVictoires + nbDefaites;
    return (total == 0) ? 0.0 : (double) nbVictoires / total;
  }

  // getters
  public int          getId()          { return id; }
  public String       getNom()         { return nom; }
  public String       getJeu()         { return jeu; }
  public List<Joueur> getJoueurs()     { return joueurs; }
  public Coach        getCoach()       { return coach; }
  public Joueur       getCapitaine()   { return capitaine; }
  public int          getNbVictoires() { return nbVictoires; }
  public int          getNbDefaites()  { return nbDefaites; }

  // setters
  public void setNom(String nom)          { this.nom = nom; }
  public void setJeu(String jeu)          { this.jeu = jeu; }
  public void setNbVictoires(int nb)      { this.nbVictoires = nb; }
  public void setNbDefaites(int nb)       { this.nbDefaites = nb; }
  public void setCapitaine(Joueur joueur) { this.capitaine = joueur; }

  @Override
  public String toString() {
    return "[ID:" + id + "] " + nom +
           " | Jeu: " + jeu +
           " | Joueurs: " + joueurs.size() + "/" + MAX_JOUEURS +
           " | V:" + nbVictoires + " D:" + nbDefaites;
  }
}
