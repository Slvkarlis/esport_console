// ===== service/GestionUtilisateur.java =====
package service;

import exception.EsportException;
import model.Administrateur;
import model.Coach;
import model.Joueur;
import model.Utilisateur;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des utilisateurs.
 * Agit comme une "base de données" en mémoire via une ArrayList.
 */
public class GestionUtilisateur {

    /** Base de données en mémoire. */
    private ArrayList<Utilisateur> utilisateurs = new ArrayList<>();

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Ajoute un utilisateur.
     * Lance une exception si le pseudo est déjà utilisé.
     */
    public void ajouter(Utilisateur u) throws EsportException {
        if (u == null) throw new EsportException("L'utilisateur ne peut pas être null.");
        for (Utilisateur ex : utilisateurs) {
            if (ex.getPseudo().equalsIgnoreCase(u.getPseudo())) {
                throw new EsportException(
                    "Un utilisateur avec le pseudo '" + u.getPseudo() + "' existe déjà.");
            }
        }
        utilisateurs.add(u);
    }

    /**
     * Modifie pseudo, email et niveau d'un utilisateur identifié par son ID.
     * Vérifie que le nouveau pseudo n'est pas déjà pris par un autre utilisateur.
     */
    public void modifier(int id, String pseudo, String email, String niveau) throws EsportException {
        Utilisateur u = rechercherParId(id);
        // Vérifie l'unicité du pseudo seulement s'il change
        if (!u.getPseudo().equalsIgnoreCase(pseudo)) {
            for (Utilisateur ex : utilisateurs) {
                if (ex.getId() != id && ex.getPseudo().equalsIgnoreCase(pseudo)) {
                    throw new EsportException("Le pseudo '" + pseudo + "' est déjà utilisé.");
                }
            }
        }
        u.setPseudo(pseudo);
        u.setEmail(email);
        u.setNiveau(niveau);
    }

    /**
     * Supprime un utilisateur par son ID.
     * Lance une exception si l'ID est introuvable.
     */
    public void supprimer(int id) throws EsportException {
        Utilisateur u = rechercherParId(id);
        utilisateurs.remove(u);
    }

    /** Retourne une copie de la liste de tous les utilisateurs. */
    public List<Utilisateur> afficherTous() {
        return new ArrayList<>(utilisateurs);
    }

    // ─── Recherche ────────────────────────────────────────────────────────────

    /** Recherche un utilisateur par pseudo (insensible à la casse). */
    public Utilisateur rechercherParPseudo(String pseudo) throws EsportException {
        if (pseudo == null) throw new EsportException("Le pseudo de recherche est null.");
        for (Utilisateur u : utilisateurs) {
            if (u.getPseudo().equalsIgnoreCase(pseudo.trim())) return u;
        }
        throw new EsportException("Aucun utilisateur trouvé avec le pseudo : '" + pseudo + "'.");
    }

    /** Recherche un utilisateur par ID. */
    public Utilisateur rechercherParId(int id) throws EsportException {
        for (Utilisateur u : utilisateurs) {
            if (u.getId() == id) return u;
        }
        throw new EsportException("Aucun utilisateur trouvé avec l'ID : " + id + ".");
    }

    // ─── Filtres par type ─────────────────────────────────────────────────────

    /** Retourne uniquement les joueurs. */
    public List<Joueur> getJoueurs() {
        List<Joueur> joueurs = new ArrayList<>();
        for (Utilisateur u : utilisateurs) {
            if (u instanceof Joueur) joueurs.add((Joueur) u);
        }
        return joueurs;
    }

    /** Retourne uniquement les coachs. */
    public List<Coach> getCoachs() {
        List<Coach> coachs = new ArrayList<>();
        for (Utilisateur u : utilisateurs) {
            if (u instanceof Coach) coachs.add((Coach) u);
        }
        return coachs;
    }

    /** Retourne uniquement les administrateurs. */
    public List<Administrateur> getAdministrateurs() {
        List<Administrateur> admins = new ArrayList<>();
        for (Utilisateur u : utilisateurs) {
            if (u instanceof Administrateur) admins.add((Administrateur) u);
        }
        return admins;
    }

    // ─── Accès direct à la collection (pour la persistance) ─────────────────
    public ArrayList<Utilisateur> getUtilisateurs() { return utilisateurs; }

    public void setUtilisateurs(ArrayList<Utilisateur> utilisateurs) {
        this.utilisateurs = (utilisateurs != null) ? utilisateurs : new ArrayList<>();
    }
}
