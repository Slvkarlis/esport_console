// ===== ui/MenuMatch.java =====
package ui;

import exception.EsportException;
import model.*;
import service.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu de gestion des matchs et des performances.
 */
public class MenuMatch extends Menu {

    private final GestionMatch        serviceMatch;
    private final GestionTournoi      serviceTournoi;
    private final GestionEquipe       serviceEquipe;
    private final GestionPerformance  servicePerformance;
    private final GestionUtilisateur  serviceUtilisateur;

    public MenuMatch(Scanner scanner,
                     GestionMatch serviceMatch,
                     GestionTournoi serviceTournoi,
                     GestionEquipe serviceEquipe,
                     GestionPerformance servicePerformance,
                     GestionUtilisateur serviceUtilisateur) {
        super(scanner);
        this.serviceMatch        = serviceMatch;
        this.serviceTournoi      = serviceTournoi;
        this.serviceEquipe       = serviceEquipe;
        this.servicePerformance  = servicePerformance;
        this.serviceUtilisateur  = serviceUtilisateur;
    }

    @Override
    public void afficher() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║           GESTION DES MATCHS          ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║  1. Créer un match                    ║");
            System.out.println("║  2. Supprimer un match                ║");
            System.out.println("║  3. Afficher tous les matchs          ║");
            System.out.println("║  4. Enregistrer un résultat           ║");
            System.out.println("║  5. Générer matchs d'un tournoi       ║");
            System.out.println("║  6. Enregistrer une performance       ║");
            System.out.println("║  7. Voir performances d'un joueur     ║");
            System.out.println("║  8. Voir performances d'un match      ║");
            System.out.println("║  0. Retour                            ║");
            System.out.println("╚═══════════════════════════════════════╝");

            int choix = lireChoix();
            try {
                switch (choix) {
                    case 1: creerMatch();                     break;
                    case 2: supprimerMatch();                 break;
                    case 3: afficherTous();                   break;
                    case 4: enregistrerResultat();            break;
                    case 5: genererMatchsTournoi();           break;
                    case 6: enregistrerPerformance();         break;
                    case 7: voirPerformancesJoueur();         break;
                    case 8: voirPerformancesMatch();          break;
                    case 0: return;
                    default: System.out.println("  Choix invalide.");
                }
            } catch (EsportException e) {
                afficherErreur(e.getMessage());
            } catch (IllegalArgumentException e) {
                afficherErreur("Saisie invalide : " + e.getMessage());
            }
            pause();
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private void creerMatch() throws EsportException {
        System.out.println("\n  ── Créer un match ──");
        int equipe1Id  = lireEntier("  ID équipe 1    : ");
        int equipe2Id  = lireEntier("  ID équipe 2    : ");
        String tournoi = lireLigne("  Nom du tournoi (vide si hors-tournoi) : ");

        Equipe e1 = serviceEquipe.rechercherParId(equipe1Id);
        Equipe e2 = serviceEquipe.rechercherParId(equipe2Id);

        Match m = new Match(e1, e2, LocalDateTime.now(), tournoi);
        serviceMatch.ajouter(m);
        afficherSucces("Match créé (ID:" + m.getId() + ") : " + e1.getNom() + " vs " + e2.getNom());
    }

    private void supprimerMatch() throws EsportException {
        System.out.println("\n  ── Supprimer un match ──");
        int id = lireEntier("  ID match : ");
        serviceMatch.supprimer(id);
        afficherSucces("Match #" + id + " supprimé.");
    }

    private void afficherTous() {
        List<Match> liste = serviceMatch.afficherTous();
        if (liste.isEmpty()) {
            System.out.println("\n  Aucun match enregistré.");
            return;
        }
        System.out.println("\n  ── Liste des matchs (" + liste.size() + ") ──");
        separateur();
        for (Match m : liste) {
            System.out.println("  " + m);
        }
        separateur();
    }

    private void enregistrerResultat() throws EsportException {
        System.out.println("\n  ── Enregistrer un résultat ──");
        int matchId = lireEntier("  ID match    : ");
        int score1  = lireEntier("  Score équipe 1 : ");
        int score2  = lireEntier("  Score équipe 2 : ");

        serviceMatch.enregistrerResultat(matchId, score1, score2);
        Match m = serviceMatch.rechercherParId(matchId);
        String resultat = m.estEgalite() ? "Égalité !"
                : "Vainqueur : " + m.getGagnant().getNom();
        afficherSucces("Résultat enregistré. " + resultat);
    }

    private void genererMatchsTournoi() throws EsportException {
        System.out.println("\n  ── Générer matchs d'un tournoi ──");
        int tournoiId = lireEntier("  ID tournoi : ");
        Tournoi t = serviceTournoi.rechercherParId(tournoiId);

        List<Match> nouveaux = serviceMatch.genererMatchs(t);
        afficherSucces(nouveaux.size() + " match(s) générés pour le tournoi '" + t.getNom() + "'.");
        for (Match m : nouveaux) {
            System.out.println("  → " + m);
        }
    }

    private void enregistrerPerformance() throws EsportException {
        System.out.println("\n  ── Enregistrer une performance ──");
        int joueurId = lireEntier("  ID joueur   : ");
        int matchId  = lireEntier("  ID match    : ");

        Utilisateur u = serviceUtilisateur.rechercherParId(joueurId);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + joueurId + " n'est pas un joueur.");
        }
        Match match = serviceMatch.rechercherParId(matchId);

        int    kills     = lireEntier("  Kills          : ");
        int    deaths    = lireEntier("  Deaths         : ");
        int    assists   = lireEntier("  Assists        : ");
        int    dmgInflig = lireEntier("  Dommages infligés  : ");
        int    dmgSubis  = lireEntier("  Dommages subis     : ");
        double precision = lireDouble("  Précision tirs (%) : ");

        Performance p = servicePerformance.enregistrerPerformance(
                (Joueur) u, match, kills, deaths, assists, dmgInflig, dmgSubis, precision);
        afficherSucces("Performance enregistrée (ID:" + p.getId() + "). KDA=" +
                String.format("%.2f", p.calculerKDA()) + " | Score=" +
                String.format("%.1f", p.calculerScorePerformance()));
    }

    private void voirPerformancesJoueur() throws EsportException {
        System.out.println("\n  ── Performances d'un joueur ──");
        int joueurId = lireEntier("  ID joueur : ");
        Utilisateur u = serviceUtilisateur.rechercherParId(joueurId);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + joueurId + " n'est pas un joueur.");
        }
        List<Performance> perfs = servicePerformance.getPerformancesJoueur((Joueur) u);
        if (perfs.isEmpty()) {
            System.out.println("  Aucune performance enregistrée.");
            return;
        }
        separateur();
        for (Performance p : perfs) {
            System.out.println("  " + p);
        }
        separateur();
    }

    private void voirPerformancesMatch() throws EsportException {
        System.out.println("\n  ── Performances d'un match ──");
        int matchId = lireEntier("  ID match : ");
        Match match = serviceMatch.rechercherParId(matchId);
        List<Performance> perfs = servicePerformance.getPerformancesMatch(match);
        if (perfs.isEmpty()) {
            System.out.println("  Aucune performance enregistrée pour ce match.");
            return;
        }
        separateur();
        for (Performance p : perfs) {
            System.out.println("  " + p);
        }
        separateur();
    }
}
