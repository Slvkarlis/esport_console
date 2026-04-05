// ===== service/GestionMatch.java =====
package service;

import exception.EsportException;
import model.Equipe;
import model.Match;
import model.Tournoi;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des matchs.
 * Gère le CRUD et la génération automatique de matchs pour un tournoi.
 */
public class GestionMatch {

    private ArrayList<Match> matchs = new ArrayList<>();

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** Ajoute un match à la liste. */
    public void ajouter(Match match) throws EsportException {
        if (match == null) throw new EsportException("Le match ne peut pas être null.");
        matchs.add(match);
    }

    /**
     * Supprime un match.
     * Interdit la suppression d'un match déjà terminé.
     */
    public void supprimer(int id) throws EsportException {
        Match m = rechercherParId(id);
        if (m.estTermine()) {
            throw new EsportException(
                "Impossible de supprimer le match #" + id + " car il est terminé.");
        }
        matchs.remove(m);
    }

    /** Retourne une copie de tous les matchs. */
    public List<Match> afficherTous() {
        return new ArrayList<>(matchs);
    }

    /** Recherche un match par ID. */
    public Match rechercherParId(int id) throws EsportException {
        for (Match m : matchs) {
            if (m.getId() == id) return m;
        }
        throw new EsportException("Aucun match trouvé avec l'ID : " + id + ".");
    }

    // ─── Résultat ─────────────────────────────────────────────────────────────

    /**
     * Enregistre le résultat d'un match.
     * Lance une exception si le match est déjà terminé.
     */
    public void enregistrerResultat(int matchId, int score1, int score2) throws EsportException {
        Match m = rechercherParId(matchId);
        if (m.estTermine()) {
            throw new EsportException("Le match #" + matchId + " est déjà terminé.");
        }
        m.enregistrerResultat(score1, score2);
    }

    // ─── Génération automatique ───────────────────────────────────────────────

    /**
     * Génère tous les matchs d'un tournoi en round-robin (chaque équipe joue contre toutes les autres).
     * Chaque match est espacé de 2 heures à partir de maintenant.
     * Les matchs sont ajoutés à la liste globale ET au tournoi.
     */
    public List<Match> genererMatchs(Tournoi tournoi) throws EsportException {
        if (tournoi == null) throw new EsportException("Le tournoi ne peut pas être null.");
        List<Equipe> equipes = tournoi.getEquipes();
        if (equipes.size() < 2) {
            throw new EsportException(
                "Il faut au moins 2 équipes pour générer des matchs (tournoi: " + tournoi.getNom() + ").");
        }
        List<Match> nouveauxMatchs = new ArrayList<>();
        LocalDateTime date = LocalDateTime.now();

        // Algorithme round-robin : chaque paire unique d'équipes joue un match
        for (int i = 0; i < equipes.size() - 1; i++) {
            for (int j = i + 1; j < equipes.size(); j++) {
                Match m = new Match(equipes.get(i), equipes.get(j), date, tournoi.getNom());
                date = date.plusHours(2); // espace de 2h entre chaque match
                matchs.add(m);
                tournoi.ajouterMatch(m);
                nouveauxMatchs.add(m);
            }
        }
        return nouveauxMatchs;
    }

    // ─── Filtres ──────────────────────────────────────────────────────────────

    /** Retourne tous les matchs appartenant à un tournoi donné (par nom). */
    public List<Match> getMatchsDuTournoi(String tournoiNom) {
        List<Match> result = new ArrayList<>();
        if (tournoiNom == null) return result;
        for (Match m : matchs) {
            if (m.getTournoiNom().equalsIgnoreCase(tournoiNom)) result.add(m);
        }
        return result;
    }

    // ─── Accès direct (persistance) ───────────────────────────────────────────
    public ArrayList<Match> getMatchs() { return matchs; }
    public void setMatchs(ArrayList<Match> matchs) {
        this.matchs = (matchs != null) ? matchs : new ArrayList<>();
    }
}
