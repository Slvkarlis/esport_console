// ===== ui/MenuEquipe.java =====
package ui;

import exception.EsportException;
import model.*;
import service.GestionEquipe;
import service.GestionUtilisateur;
import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu de gestion des équipes.
 */
public class MenuEquipe extends Menu {

    private final GestionEquipe serviceEquipe;
    private final GestionUtilisateur serviceUtilisateur;

    public MenuEquipe(Scanner scanner, GestionEquipe serviceEquipe,
                      GestionUtilisateur serviceUtilisateur) {
        super(scanner);
        this.serviceEquipe = serviceEquipe;
        this.serviceUtilisateur = serviceUtilisateur;
    }

    @Override
    public void afficher() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║          GESTION DES ÉQUIPES          ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║  1. Créer une équipe                  ║");
            System.out.println("║  2. Modifier une équipe               ║");
            System.out.println("║  3. Supprimer une équipe              ║");
            System.out.println("║  4. Afficher toutes les équipes       ║");
            System.out.println("║  5. Ajouter un joueur à une équipe    ║");
            System.out.println("║  6. Retirer un joueur d'une équipe    ║");
            System.out.println("║  7. Affecter un coach à une équipe    ║");
            System.out.println("║  8. Définir le capitaine              ║");
            System.out.println("║  9. Détails d'une équipe              ║");
            System.out.println("║  0. Retour                            ║");
            System.out.println("╚═══════════════════════════════════════╝");

            int choix = lireChoix();
            try {
                switch (choix) {
                    case 1: creerEquipe();                break;
                    case 2: modifierEquipe();             break;
                    case 3: supprimerEquipe();            break;
                    case 4: afficherToutes();             break;
                    case 5: ajouterJoueurEquipe();        break;
                    case 6: retirerJoueurEquipe();        break;
                    case 7: affecterCoach();              break;
                    case 8: definirCapitaine();           break;
                    case 9: detailsEquipe();              break;
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

    private void creerEquipe() throws EsportException {
        System.out.println("\n  ── Créer une équipe ──");
        String nom = lireLigne("  Nom de l'équipe : ");
        String jeu = lireLigne("  Jeu             : ");

        Equipe e = new Equipe(nom, jeu);
        serviceEquipe.ajouter(e);
        afficherSucces("Équipe '" + nom + "' créée (ID:" + e.getId() + ").");
    }

    private void modifierEquipe() throws EsportException {
        System.out.println("\n  ── Modifier une équipe ──");
        int    id  = lireEntier("  ID équipe        : ");
        String nom = lireLigne("  Nouveau nom      : ");
        String jeu = lireLigne("  Nouveau jeu      : ");

        serviceEquipe.modifier(id, nom, jeu);
        afficherSucces("Équipe #" + id + " mise à jour.");
    }

    private void supprimerEquipe() throws EsportException {
        System.out.println("\n  ── Supprimer une équipe ──");
        int id = lireEntier("  ID équipe à supprimer : ");
        serviceEquipe.supprimer(id);
        afficherSucces("Équipe #" + id + " supprimée.");
    }

    private void afficherToutes() {
        List<Equipe> liste = serviceEquipe.afficherToutes();
        if (liste.isEmpty()) {
            System.out.println("\n  Aucune équipe enregistrée.");
            return;
        }
        System.out.println("\n  ── Liste des équipes (" + liste.size() + ") ──");
        separateur();
        for (Equipe e : liste) {
            System.out.println("  " + e);
        }
        separateur();
    }

    private void ajouterJoueurEquipe() throws EsportException {
        System.out.println("\n  ── Ajouter un joueur à une équipe ──");
        int equipeId = lireEntier("  ID équipe  : ");
        int joueurId = lireEntier("  ID joueur  : ");

        Utilisateur u = serviceUtilisateur.rechercherParId(joueurId);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + joueurId + " n'est pas un joueur.");
        }
        serviceEquipe.ajouterJoueurAEquipe(equipeId, (Joueur) u);
        afficherSucces("Joueur #" + joueurId + " ajouté à l'équipe #" + equipeId + ".");
    }

    private void retirerJoueurEquipe() throws EsportException {
        System.out.println("\n  ── Retirer un joueur d'une équipe ──");
        int equipeId = lireEntier("  ID équipe  : ");
        int joueurId = lireEntier("  ID joueur  : ");

        Utilisateur u = serviceUtilisateur.rechercherParId(joueurId);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + joueurId + " n'est pas un joueur.");
        }
        serviceEquipe.retirerJoueurDeEquipe(equipeId, (Joueur) u);
        afficherSucces("Joueur #" + joueurId + " retiré de l'équipe #" + equipeId + ".");
    }

    private void affecterCoach() throws EsportException {
        System.out.println("\n  ── Affecter un coach à une équipe ──");
        int equipeId = lireEntier("  ID équipe  : ");
        int coachId  = lireEntier("  ID coach   : ");

        Utilisateur u = serviceUtilisateur.rechercherParId(coachId);
        if (!(u instanceof Coach)) {
            throw new EsportException("L'utilisateur #" + coachId + " n'est pas un coach.");
        }
        Coach coach = (Coach) u;
        if (!coach.estDisponible()) {
            System.out.println("  [ATTENTION] Ce coach entraîne déjà l'équipe '"
                    + coach.getEquipeCoachee() + "'. Continuer ? (o/n)");
            String rep = scanner.nextLine().trim();
            if (!"o".equalsIgnoreCase(rep)) return;
        }
        serviceEquipe.affecterCoach(equipeId, coach);
        afficherSucces("Coach #" + coachId + " affecté à l'équipe #" + equipeId + ".");
    }

    private void definirCapitaine() throws EsportException {
        System.out.println("\n  ── Définir le capitaine ──");
        int equipeId = lireEntier("  ID équipe  : ");
        int joueurId = lireEntier("  ID joueur  : ");

        Utilisateur u = serviceUtilisateur.rechercherParId(joueurId);
        if (!(u instanceof Joueur)) {
            throw new EsportException("L'utilisateur #" + joueurId + " n'est pas un joueur.");
        }
        serviceEquipe.definirCapitaine(equipeId, (Joueur) u);
        afficherSucces("Joueur #" + joueurId + " défini comme capitaine de l'équipe #" + equipeId + ".");
    }

    private void detailsEquipe() throws EsportException {
        System.out.println("\n  ── Détails d'une équipe ──");
        int id = lireEntier("  ID équipe : ");
        Equipe e = serviceEquipe.rechercherParId(id);

        System.out.println("\n  " + e);
        System.out.println("  Coach     : " + (e.getCoach() != null
                ? e.getCoach().getPseudo() + " (" + e.getCoach().getSpecialite() + ")" : "Aucun"));
        System.out.println("  Capitaine : " + (e.getCapitaine() != null
                ? e.getCapitaine().getPseudo() : "Non défini"));
        System.out.println("  Joueurs   :");
        List<Joueur> joueurs = e.getJoueurs();
        if (joueurs.isEmpty()) {
            System.out.println("    (aucun joueur)");
        } else {
            for (Joueur j : joueurs) {
                System.out.printf("    - %-12s | %s | %d pts%n",
                        j.getPseudo(), j.getRang(), j.getScoreTotal());
            }
        }
        System.out.printf("  Ratio victoires : %.1f%%%n", e.getRatioVictoires() * 100);
    }
}
