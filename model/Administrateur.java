package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Administrateur herite de Utilisateur
public class Administrateur extends Utilisateur {

  // attributs specifiques a l'administrateur
  private String       roleAdmin;
  private List<String> permissions;
  private int          nbActionsEffectuees;

  // constructeur
  public Administrateur(String pseudo, String email, String niveau, String roleAdmin) {
    super(pseudo, email, niveau);
    this.roleAdmin = roleAdmin;
    this.permissions = new ArrayList<>();
    this.nbActionsEffectuees = 0;
  }

  // constructeur pour chargement depuis fichier
  public Administrateur(int id, String pseudo, String email, String niveau, LocalDate dateIns,
                        String roleAdmin, List<String> permissions, int nbActionsEffectuees) {
    super(id, pseudo, email, niveau, dateIns);
    this.roleAdmin = roleAdmin;
    this.permissions = permissions;
    this.nbActionsEffectuees = nbActionsEffectuees;
  }

  // verifie si l'admin possede une permission donnee
  public boolean aPermission(String permission) {
    return permissions.contains(permission);
  }

  // ajoute une permission si elle n'existe pas deja
  public void ajouterPermission(String permission) {
    if (permission != null && !permission.isEmpty() && !permissions.contains(permission))
      permissions.add(permission);
  }

  // incremente le compteur d'actions effectuees
  public void enregistrerAction() {
    nbActionsEffectuees++;
  }

  // methode abstraite implementee — presentation de l'administrateur
  @Override
  public String sePresenter() {
    return "Administrateur: " + getPseudo() +
           " | Role: " + roleAdmin +
           " | Permissions: " + (permissions.isEmpty() ? "Aucune" : String.join(", ", permissions)) +
           " | Actions: " + nbActionsEffectuees;
  }

  // retourne le role de cet utilisateur
  @Override
  public String getRole() { return "Administrateur"; }

  // getters
  public String       getRoleAdmin()           { return roleAdmin; }
  public List<String> getPermissions()         { return permissions; }
  public int          getNbActionsEffectuees() { return nbActionsEffectuees; }

  // setters
  public void setRoleAdmin(String roleAdmin)             { this.roleAdmin = roleAdmin; }
  public void setNbActionsEffectuees(int nbActions)      { this.nbActionsEffectuees = nbActions; }

  @Override
  public String toString() {
    return "[ID:" + getId() + "] " + getPseudo() +
           " | Admin | " + roleAdmin;
  }
}
