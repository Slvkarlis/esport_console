// ===== service/GestionClassement.java =====
package service;

import model.Classement;
import model.Equipe;
import model.Joueur;
import model.Performance;
import java.util.List;

/**
 * Service de classement.
 * Agrège les données des autres services pour produire les classements
 * et déterminer le MVP.
 */
public class GestionClassement {

    private final GestionUtilisateur gestionUtilisateur;
    private final GestionEquipe gestionEquipe;
    private final GestionPerformance gestionPerformance;

    public GestionClassement(GestionUtilisateur gestionUtilisateur,
                              GestionEquipe gestionEquipe,
                              GestionPerformance gestionPerformance) {
        this.gestionUtilisateur = gestionUtilisateur;
        this.gestionEquipe = gestionEquipe;
        this.gestionPerformance = gestionPerformance;
    }

    // ─── Classements ──────────────────────────────────────────────────────────

    /** Retourne le classement formaté des joueurs par score. */
    public String classementJoueurs() {
        List<Joueur> joueurs = gestionUtilisateur.getJoueurs();
        if (joueurs.isEmpty()) {
            return "Aucun joueur enregistré pour établir un classement.";
        }
        return Classement.afficherClassementJoueurs(joueurs);
    }

    /** Retourne le classement formaté des équipes par victoires. */
    public String classementEquipes() {
        List<Equipe> equipes = gestionEquipe.afficherToutes();
        if (equipes.isEmpty()) {
            return "Aucune équipe enregistrée pour établir un classement.";
        }
        return Classement.afficherClassementEquipes(equipes);
    }

    /**
     * Détermine et retourne une description du MVP global.
     * Le MVP est le joueur avec le meilleur score de performance toutes performances confondues.
     */
    public String determinerMVP() {
        List<Performance> performances = gestionPerformance.afficherToutes();
        if (performances.isEmpty()) {
            return "Aucune performance enregistrée — impossible de déterminer le MVP.";
        }
        Joueur mvp = Classement.determinerMVP(performances);
        if (mvp == null) {
            return "Impossible de déterminer le MVP.";
        }
        return String.format(
            "╔══════════════════════════════╗%n" +
            "║          MVP GLOBAL          ║%n" +
            "╠══════════════════════════════╣%n" +
            "║ Joueur  : %-18s ║%n" +
            "║ Rang    : %-18s ║%n" +
            "║ Score   : %-18d ║%n" +
            "╚══════════════════════════════╝",
            mvp.getPseudo(), mvp.getRang(), mvp.getScoreTotal());
    }

    /** Retourne les stats détaillées d'un joueur à partir de ses performances. */
    public String statsJoueur(Joueur joueur) {
        List<Performance> perfs = gestionPerformance.getPerformancesJoueur(joueur);
        if (perfs.isEmpty()) {
            return "Aucune performance enregistrée pour " + joueur.getPseudo() + ".";
        }
        double kdaTotal = perfs.stream().mapToDouble(Performance::calculerKDA).average().orElse(0);
        double scoreMoyen = perfs.stream()
                .mapToDouble(Performance::calculerScorePerformance).average().orElse(0);
        int killsTotal = perfs.stream().mapToInt(Performance::getKills).sum();
        int deathsTotal = perfs.stream().mapToInt(Performance::getDeaths).sum();

        return String.format(
            "Stats de %s (%d matchs joués) :%n" +
            "  KDA moyen    : %.2f%n" +
            "  Score moyen  : %.1f%n" +
            "  Total kills  : %d  |  Total deaths : %d",
            joueur.getPseudo(), perfs.size(), kdaTotal, scoreMoyen, killsTotal, deathsTotal);
    }
}
