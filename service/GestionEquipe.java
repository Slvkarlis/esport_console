// ===== service/GestionEquipe.java =====
package service;

import exception.EsportException;
import exception.EquipeIntrouvableException;
import model.Coach;
import model.Equipe;
import model.Joueur;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des équipes.
 * Délègue la logique complexe aux méthodes de la classe Equipe.
 */
public class GestionEquipe {

    private ArrayList<Equipe> equipes = new ArrayList<>();

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** Ajoute une équipe. Lance une exception si le nom est déjà utilisé. */
    public void ajouter(Equipe equipe) throws EsportException {
        if (equipe == null) throw new EsportException("L'équipe ne peut pas être null.");
        for (Equipe e : equipes) {
            if (e.getNom().equalsIgnoreCase(equipe.getNom())) {
                throw new EsportException(
                    "Une équipe avec le nom '" + equipe.getNom() + "' existe déjà.");
            }
        }
        equipes.add(equipe);
    }

    /** Modifie le nom et le jeu d'une équipe. */
    public void modifier(int id, String nom, String jeu) throws EsportException {
        Equipe e = rechercherParId(id);
        // Vérifie unicité du nom si différent
        if (!e.getNom().equalsIgnoreCase(nom)) {
            for (Equipe ex : equipes) {
                if (ex.getId() != id && ex.getNom().equalsIgnoreCase(nom)) {
                    throw new EsportException("Une équipe '" + nom + "' existe déjà.");
                }
            }
        }
        e.setNom(nom);
        e.setJeu(jeu);
    }

    /** Supprime une équipe par ID. */
    public void supprimer(int id) throws EsportException {
        Equipe e = rechercherParId(id);
        equipes.remove(e);
    }

    /** Retourne une copie de toutes les équipes. */
    public List<Equipe> afficherToutes() {
        return new ArrayList<>(equipes);
    }

    // ─── Recherche ────────────────────────────────────────────────────────────

    public Equipe rechercherParId(int id) throws EsportException {
        for (Equipe e : equipes) {
            if (e.getId() == id) return e;
        }
        throw new EquipeIntrouvableException("Aucune équipe trouvée avec l'ID : " + id + ".");
    }

    public Equipe rechercherParNom(String nom) throws EsportException {
        if (nom == null) throw new EquipeIntrouvableException("Le nom de recherche est null.");
        for (Equipe e : equipes) {
            if (e.getNom().equalsIgnoreCase(nom.trim())) return e;
        }
        throw new EquipeIntrouvableException("Aucune équipe trouvée avec le nom : '" + nom + "'.");
    }

    // ─── Gestion des membres ──────────────────────────────────────────────────

    /** Ajoute un joueur à une équipe. */
    public void ajouterJoueurAEquipe(int equipeId, Joueur joueur) throws EsportException {
        rechercherParId(equipeId).ajouterJoueur(joueur);
    }

    /** Retire un joueur d'une équipe. */
    public void retirerJoueurDeEquipe(int equipeId, Joueur joueur) throws EsportException {
        rechercherParId(equipeId).retirerJoueur(joueur);
    }

    /** Affecte un coach à une équipe. */
    public void affecterCoach(int equipeId, Coach coach) throws EsportException {
        rechercherParId(equipeId).affecterCoach(coach);
    }

    /** Définit le capitaine d'une équipe. */
    public void definirCapitaine(int equipeId, Joueur joueur) throws EsportException {
        rechercherParId(equipeId).definirCapitaine(joueur);
    }

    // ─── Accès direct (persistance) ───────────────────────────────────────────
    public ArrayList<Equipe> getEquipes() { return equipes; }
    public void setEquipes(ArrayList<Equipe> equipes) {
        this.equipes = (equipes != null) ? equipes : new ArrayList<>();
    }
}
