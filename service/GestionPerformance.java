// ===== service/GestionPerformance.java =====
package service;

import exception.EsportException;
import model.Joueur;
import model.Match;
import model.Performance;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des performances des joueurs.
 * Permet d'enregistrer et de consulter les stats par joueur ou par match.
 */
public class GestionPerformance {

    private ArrayList<Performance> performances = new ArrayList<>();

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** Ajoute une performance. */
    public void ajouter(Performance p) throws EsportException {
        if (p == null) throw new EsportException("La performance ne peut pas être null.");
        performances.add(p);
    }

    /** Supprime une performance par ID. */
    public void supprimer(int id) throws EsportException {
        Performance p = rechercherParId(id);
        performances.remove(p);
    }

    /** Recherche une performance par ID. */
    public Performance rechercherParId(int id) throws EsportException {
        for (Performance p : performances) {
            if (p.getId() == id) return p;
        }
        throw new EsportException("Aucune performance trouvée avec l'ID : " + id + ".");
    }

    /** Retourne une copie de toutes les performances. */
    public List<Performance> afficherToutes() {
        return new ArrayList<>(performances);
    }

    // ─── Filtres ──────────────────────────────────────────────────────────────

    /** Retourne toutes les performances d'un joueur spécifique. */
    public List<Performance> getPerformancesJoueur(Joueur joueur) {
        List<Performance> result = new ArrayList<>();
        if (joueur == null) return result;
        for (Performance p : performances) {
            if (p.getJoueur().getId() == joueur.getId()) result.add(p);
        }
        return result;
    }

    /** Retourne toutes les performances enregistrées pour un match spécifique. */
    public List<Performance> getPerformancesMatch(Match match) {
        List<Performance> result = new ArrayList<>();
        if (match == null) return result;
        for (Performance p : performances) {
            if (p.getMatch().getId() == match.getId()) result.add(p);
        }
        return result;
    }

    // ─── Création rapide ──────────────────────────────────────────────────────

    /**
     * Crée et enregistre directement une performance pour un joueur/match donné.
     * Appelle enregistrerStats() qui met à jour le score et le rang du joueur.
     */
    public Performance enregistrerPerformance(Joueur joueur, Match match,
                                               int kills, int deaths, int assists,
                                               int dommagesInfliges, int dommagesSubis,
                                               double precisionTirs) throws EsportException {
        if (joueur == null || match == null) {
            throw new EsportException("Joueur et match doivent être définis.");
        }
        Performance p = new Performance(joueur, match);
        p.enregistrerStats(kills, deaths, assists, dommagesInfliges, dommagesSubis, precisionTirs);
        performances.add(p);
        return p;
    }

    // ─── Accès direct (persistance) ───────────────────────────────────────────
    public ArrayList<Performance> getPerformances() { return performances; }
    public void setPerformances(ArrayList<Performance> performances) {
        this.performances = (performances != null) ? performances : new ArrayList<>();
    }
}
