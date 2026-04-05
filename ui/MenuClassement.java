// ===== ui/MenuClassement.java =====
package ui;

import exception.EsportException;
import model.Joueur;
import model.Utilisateur;
import service.GestionClassement;
import service.GestionUtilisateur;
import java.util.Scanner;

/**
 * Sous-menu d'affichage des classements et du MVP.
 */
public class MenuClassement extends Menu {

    private final GestionClassement  serviceClassement;
    private final GestionUtilisateur serviceUtilisateur;

    public MenuClassement(Scanner scanner,
                          GestionClassement serviceClassement,
                          GestionUtilisateur serviceUtilisateur) {
        super(scanner);
        this.serviceClassement  = serviceClassement;
        this.serviceUtilisateur = serviceUtilisateur;
    }

    @Override
    public void afficher() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║            CLASSEMENTS                ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║  1. Classement des joueurs            ║");
            System.out.println("║  2. Classement des équipes            ║");
            System.out.println("║  3. Déterminer le MVP global          ║");
            System.out.println("║  4. Stats détaillées d'un joueur      ║");
            System.out.println("║  0. Retour                            ║");
            System.out.println("╚═══════════════════════════════════════╝");

            int choix = lireChoix();
            try {
                switch (choix) {
                    case 1: classementJoueurs();    break;
                    case 2: classementEquipes();    break;
                    case 3: mvpGlobal();            break;
                    case 4: statsJoueur();          break;
                    case 0: return;
                    default: System.out.println("  Choix invalide.");
                }
            } catch (EsportException e) {
                afficherErreur(e.getMessage());
            }
            pause();
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private void classementJoueurs() {
        System.out.println();
        System.out.println(serviceClassement.classementJoueurs());
    }

    private void classementEquipes() {
        System.out.println();
        System.out.println(serviceClassement.classementEquipes());
    }

    private void mvpGlobal() {
        System.out.println();
        System.out.println(serviceClassement.determinerMVP());
    }

    private void statsJoueur() throws EsportException {
        System.out.println("\n  ── Stats d'un joueur ──");
        int id = lireEntier("  ID joueur : ");
        Utilisateur u = serviceUtilisateur.rechercherParId(id);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + id + " n'est pas un joueur.");
        }
        System.out.println();
        System.out.println(serviceClassement.statsJoueur((Joueur) u));
    }
}
