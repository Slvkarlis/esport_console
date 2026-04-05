// ===== ui/MenuTournoi.java =====
package ui;

import exception.EsportException;
import model.*;
import service.GestionEquipe;
import service.GestionTournoi;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu de gestion des tournois.
 */
public class MenuTournoi extends Menu {

    private final GestionTournoi serviceTournoi;
    private final GestionEquipe  serviceEquipe;

    public MenuTournoi(Scanner scanner, GestionTournoi serviceTournoi, GestionEquipe serviceEquipe) {
        super(scanner);
        this.serviceTournoi = serviceTournoi;
        this.serviceEquipe  = serviceEquipe;
    }

    @Override
    public void afficher() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║         GESTION DES TOURNOIS          ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║  1. Créer un tournoi                  ║");
            System.out.println("║  2. Supprimer un tournoi              ║");
            System.out.println("║  3. Afficher tous les tournois        ║");
            System.out.println("║  4. Inscrire une équipe               ║");
            System.out.println("║  5. Démarrer un tournoi               ║");
            System.out.println("║  6. Terminer un tournoi               ║");
            System.out.println("║  7. Détails d'un tournoi              ║");
            System.out.println("║  0. Retour                            ║");
            System.out.println("╚═══════════════════════════════════════╝");

            int choix = lireChoix();
            try {
                switch (choix) {
                    case 1: creerTournoi();        break;
                    case 2: supprimerTournoi();    break;
                    case 3: afficherTous();        break;
                    case 4: inscrireEquipe();      break;
                    case 5: demarrerTournoi();     break;
                    case 6: terminerTournoi();     break;
                    case 7: detailsTournoi();      break;
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

    private void creerTournoi() throws EsportException {
        System.out.println("\n  ── Créer un tournoi ──");
        String nom = lireLigne("  Nom du tournoi  : ");
        String jeu = lireLigne("  Jeu             : ");

        System.out.println("  Format : 1=ELIMINATION  2=ROUND_ROBIN  3=SUISSE");
        int    choixFormat = lireEntier("  Choix format    : ");
        Tournoi.Format format = switch (choixFormat) {
            case 1  -> Tournoi.Format.ELIMINATION;
            case 3  -> Tournoi.Format.SUISSE;
            default -> Tournoi.Format.ROUND_ROBIN;
        };

        LocalDate dateDebut = lireDate("  Date début (AAAA-MM-JJ) : ");
        LocalDate dateFin   = lireDate("  Date fin   (AAAA-MM-JJ) : ");
        int       nbMax     = lireEntier("  Nb équipes max           : ");

        Tournoi t = new Tournoi(nom, jeu, format, dateDebut, dateFin, nbMax);
        serviceTournoi.ajouter(t);
        afficherSucces("Tournoi '" + nom + "' créé (ID:" + t.getId() + ").");
    }

    private void supprimerTournoi() throws EsportException {
        System.out.println("\n  ── Supprimer un tournoi ──");
        int id = lireEntier("  ID tournoi : ");
        serviceTournoi.supprimer(id);
        afficherSucces("Tournoi #" + id + " supprimé.");
    }

    private void afficherTous() {
        List<Tournoi> liste = serviceTournoi.afficherTous();
        if (liste.isEmpty()) {
            System.out.println("\n  Aucun tournoi enregistré.");
            return;
        }
        System.out.println("\n  ── Liste des tournois (" + liste.size() + ") ──");
        separateur();
        for (Tournoi t : liste) {
            System.out.println("  " + t);
            if (t.getVainqueur() != null) {
                System.out.println("    Vainqueur : " + t.getVainqueur().getNom());
            }
        }
        separateur();
    }

    private void inscrireEquipe() throws EsportException {
        System.out.println("\n  ── Inscrire une équipe dans un tournoi ──");
        int tournoiId = lireEntier("  ID tournoi : ");
        int equipeId  = lireEntier("  ID équipe  : ");

        Equipe equipe = serviceEquipe.rechercherParId(equipeId);
        serviceTournoi.inscrireEquipe(tournoiId, equipe);
        afficherSucces("Équipe '" + equipe.getNom() + "' inscrite au tournoi #" + tournoiId + ".");
    }

    private void demarrerTournoi() throws EsportException {
        System.out.println("\n  ── Démarrer un tournoi ──");
        int id = lireEntier("  ID tournoi : ");
        serviceTournoi.demarrerTournoi(id);
        afficherSucces("Tournoi #" + id + " démarré !");
    }

    private void terminerTournoi() throws EsportException {
        System.out.println("\n  ── Terminer un tournoi ──");
        int tournoiId = lireEntier("  ID tournoi          : ");
        int equipeId  = lireEntier("  ID équipe vainqueur : ");

        Equipe vainqueur = serviceEquipe.rechercherParId(equipeId);
        serviceTournoi.terminerTournoi(tournoiId, vainqueur);
        afficherSucces("Tournoi #" + tournoiId + " terminé ! Vainqueur : " + vainqueur.getNom());
    }

    private void detailsTournoi() throws EsportException {
        System.out.println("\n  ── Détails d'un tournoi ──");
        int id = lireEntier("  ID tournoi : ");
        Tournoi t = serviceTournoi.rechercherParId(id);

        System.out.println("\n  " + t);
        System.out.println("  Dates     : " + t.getDateDebut() + " → " + t.getDateFin());
        System.out.println("  Équipes inscrites (" + t.getEquipes().size() + ") :");
        if (t.getEquipes().isEmpty()) {
            System.out.println("    (aucune équipe)");
        } else {
            for (Equipe e : t.getEquipes()) {
                System.out.println("    - " + e.getNom() + " (" + e.getJeu() + ")");
            }
        }
        if (t.getVainqueur() != null) {
            System.out.println("  🏆 Vainqueur : " + t.getVainqueur().getNom());
        }
    }

    // ─── Lecture de date ──────────────────────────────────────────────────────

    private LocalDate lireDate(String prompt) {
        while (true) {
            String saisie = lireLigne(prompt);
            try {
                return LocalDate.parse(saisie);
            } catch (DateTimeParseException e) {
                System.out.println("  Format invalide. Utilisez AAAA-MM-JJ (ex: 2025-06-15).");
            }
        }
    }
}
