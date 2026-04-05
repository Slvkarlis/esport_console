// ===== service/GestionTournoi.java =====
package service;

import exception.EsportException;
import model.Equipe;
import model.Tournoi;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des tournois.
 * Fournit les opérations CRUD et les transitions d'état.
 */
public class GestionTournoi {

    private ArrayList<Tournoi> tournois = new ArrayList<>();

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** Ajoute un tournoi. Vérifie l'unicité du nom. */
    public void ajouter(Tournoi tournoi) throws EsportException {
        if (tournoi == null) throw new EsportException("Le tournoi ne peut pas être null.");
        for (Tournoi t : tournois) {
            if (t.getNom().equalsIgnoreCase(tournoi.getNom())) {
                throw new EsportException(
                    "Un tournoi avec le nom '" + tournoi.getNom() + "' existe déjà.");
            }
        }
        tournois.add(tournoi);
    }

    /**
     * Supprime un tournoi.
     * Interdit la suppression d'un tournoi en cours.
     */
    public void supprimer(int id) throws EsportException {
        Tournoi t = rechercherParId(id);
        if (t.getStatut() == Tournoi.Statut.EN_COURS) {
            throw new EsportException(
                "Impossible de supprimer le tournoi '" + t.getNom() + "' car il est en cours.");
        }
        tournois.remove(t);
    }

    /** Retourne une copie de tous les tournois. */
    public List<Tournoi> afficherTous() {
        return new ArrayList<>(tournois);
    }

    /** Recherche un tournoi par ID. */
    public Tournoi rechercherParId(int id) throws EsportException {
        for (Tournoi t : tournois) {
            if (t.getId() == id) return t;
        }
        throw new EsportException("Aucun tournoi trouvé avec l'ID : " + id + ".");
    }

    // ─── Transitions d'état ───────────────────────────────────────────────────

    /** Inscrit une équipe dans un tournoi. */
    public void inscrireEquipe(int tournoiId, Equipe equipe) throws EsportException {
        rechercherParId(tournoiId).inscrireEquipe(equipe);
    }

    /** Démarre un tournoi (statut → EN_COURS). */
    public void demarrerTournoi(int tournoiId) throws EsportException {
        rechercherParId(tournoiId).demarrer();
    }

    /** Termine un tournoi en désignant le vainqueur. */
    public void terminerTournoi(int tournoiId, Equipe vainqueur) throws EsportException {
        rechercherParId(tournoiId).terminer(vainqueur);
    }

    // ─── Accès direct (persistance) ───────────────────────────────────────────
    public ArrayList<Tournoi> getTournois() { return tournois; }
    public void setTournois(ArrayList<Tournoi> tournois) {
        this.tournois = (tournois != null) ? tournois : new ArrayList<>();
    }
}
