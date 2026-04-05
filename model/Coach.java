package model;

import java.time.LocalDate;

// Coach herite de Utilisateur
public class Coach extends Utilisateur {

  // attributs specifiques au coach
  private String specialite;
  private int    anneesExp;
  private String equipeCoachee;      // nom de l'equipe actuellement coachee
  private int    nbVictoiresCoachees;

  // constructeur
  public Coach(String pseudo, String email, String niveau, String specialite, int anneesExp) {
    super(pseudo, email, niveau);
    this.specialite = specialite;
    this.anneesExp = anneesExp;
    this.equipeCoachee = "";
    this.nbVictoiresCoachees = 0;
  }

  // constructeur pour chargement depuis fichier
  public Coach(int id, String pseudo, String email, String niveau, LocalDate dateIns,
               String specialite, int anneesExp, String equipeCoachee, int nbVictoiresCoachees) {
    super(id, pseudo, email, niveau, dateIns);
    this.specialite = specialite;
    this.anneesExp = anneesExp;
    this.equipeCoachee = equipeCoachee;
    this.nbVictoiresCoachees = nbVictoiresCoachees;
  }

  // incremente le compteur de victoires coachees
  public void enregistrerVictoire() {
    nbVictoiresCoachees++;
  }

  // un coach est disponible s'il n'entraine aucune equipe
  public boolean estDisponible() {
    return equipeCoachee == null || equipeCoachee.isEmpty();
  }

  // methode abstraite implementee — presentation du coach
  @Override
  public String sePresenter() {
    return "Coach: " + getPseudo() +
           " | Specialite: " + specialite +
           " | Exp: " + anneesExp + " an(s)" +
           " | Equipe: " + (estDisponible() ? "Disponible" : equipeCoachee) +
           " | Victoires: " + nbVictoiresCoachees;
  }

  // retourne le role de cet utilisateur
  @Override
  public String getRole() { return "Coach"; }

  // getters
  public String getSpecialite()          { return specialite; }
  public int    getAnneesExp()           { return anneesExp; }
  public String getEquipeCoachee()       { return equipeCoachee; }
  public int    getNbVictoiresCoachees() { return nbVictoiresCoachees; }

  // setters
  public void setSpecialite(String specialite)       { this.specialite = specialite; }
  public void setAnneesExp(int anneesExp)            { this.anneesExp = anneesExp; }
  public void setEquipeCoachee(String equipeCoachee) { this.equipeCoachee = equipeCoachee; }
  public void setNbVictoiresCoachees(int nb)         { this.nbVictoiresCoachees = nb; }

  @Override
  public String toString() {
    return "[ID:" + getId() + "] " + getPseudo() +
           " | Coach | " + specialite + " | " + anneesExp + " an(s) exp.";
  }
}
