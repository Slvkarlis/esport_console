
// ===== ui/MenuUtilisateur.java =====
package ui;

import exception.EsportException;
import model.*;
import service.GestionUtilisateur;
import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu de gestion des utilisateurs (joueurs, coachs, administrateurs).
 */
public class MenuUtilisateur extends Menu {

    private static final String PROMPT_NIVEAU = "  Niveau : ";

    private final GestionUtilisateur service;

    public MenuUtilisateur(Scanner scanner, GestionUtilisateur service) {
        super(scanner);
        this.service = service;
    }

    @Override
    public void afficher() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║       GESTION DES UTILISATEURS        ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║  1. Ajouter un joueur                 ║");
            System.out.println("║  2. Ajouter un coach                  ║");
            System.out.println("║  3. Ajouter un administrateur         ║");
            System.out.println("║  4. Modifier un utilisateur           ║");
            System.out.println("║  5. Supprimer un utilisateur          ║");
            System.out.println("║  6. Afficher tous les utilisateurs    ║");
            System.out.println("║  7. Rechercher par pseudo             ║");
            System.out.println("║  8. Afficher la fiche d'un utilisateur║");
            System.out.println("║  0. Retour au menu principal          ║");
            System.out.println("╚═══════════════════════════════════════╝");

            int choix = lireChoix();
            try {
                switch (choix) {
                    case 1: ajouterJoueur();         break;
                    case 2: ajouterCoach();          break;
                    case 3: ajouterAdmin();          break;
                    case 4: modifierUtilisateur();   break;
                    case 5: supprimerUtilisateur();  break;
                    case 6: afficherTous();          break;
                    case 7: rechercherParPseudo();   break;
                    case 8: afficherFiche();         break;
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

    private void ajouterJoueur() throws EsportException {
        System.out.println("\n  ── Ajouter un joueur ──");
        String pseudo = lireLigne("  Pseudo        : ");
        String email  = lireLigne("  Email         : ");
        String niveau = lireLigne(PROMPT_NIVEAU);
        String jeu    = lireLigne("  Jeu principal : ");

        Joueur j = new Joueur(pseudo, email, niveau, jeu);
        service.ajouter(j);
        afficherSucces("Joueur '" + pseudo + "' ajouté (ID:" + j.getId() + ").");
    }

    private void ajouterCoach() throws EsportException {
        System.out.println("\n  ── Ajouter un coach ──");
        String pseudo = lireLigne("  Pseudo        : ");
        String email  = lireLigne("  Email         : ");
        String niveau = lireLigne(PROMPT_NIVEAU);
        String spec   = lireLigne("  Spécialité    : ");
        int    exp    = lireEntier("  Années exp.   : ");

        Coach c = new Coach(pseudo, email, niveau, spec, exp);
        service.ajouter(c);
        afficherSucces("Coach '" + pseudo + "' ajouté (ID:" + c.getId() + ").");
    }

    private void ajouterAdmin() throws EsportException {
        System.out.println("\n  ── Ajouter un administrateur ──");
        String pseudo = lireLigne("  Pseudo        : ");
        String email  = lireLigne("  Email         : ");
        String niveau = lireLigne(PROMPT_NIVEAU);
        String role   = lireLigne("  Rôle admin    : ");

        Administrateur a = new Administrateur(pseudo, email, niveau, role);
        // Proposition d'ajout de permissions initiales
        String perms = lireLigne("  Permissions (séparées par , ou vide) : ");
        if (!perms.isEmpty()) {
            for (String perm : perms.split(",")) {
                a.ajouterPermission(perm.trim());
            }
        }
        service.ajouter(a);
        afficherSucces("Administrateur '" + pseudo + "' ajouté (ID:" + a.getId() + ").");
    }

    private void modifierUtilisateur() throws EsportException {
        System.out.println("\n  ── Modifier un utilisateur ──");
        int    id     = lireEntier("  ID utilisateur : ");
        String pseudo = lireLigne("  Nouveau pseudo  : ");
        String email  = lireLigne("  Nouvel email    : ");
        String niveau = lireLigne("  Nouveau niveau  : ");

        service.modifier(id, pseudo, email, niveau);
        afficherSucces("Utilisateur #" + id + " mis à jour.");
    }

    private void supprimerUtilisateur() throws EsportException {
        System.out.println("\n  ── Supprimer un utilisateur ──");
        int id = lireEntier("  ID utilisateur à supprimer : ");
        service.supprimer(id);
        afficherSucces("Utilisateur #" + id + " supprimé.");
    }

    private void afficherTous() {
        List<Utilisateur> liste = service.afficherTous();
        if (liste.isEmpty()) {
            System.out.println("\n  Aucun utilisateur enregistré.");
            return;
        }
        System.out.println("\n  ── Liste des utilisateurs (" + liste.size() + ") ──");
        separateur();
        for (Utilisateur u : liste) {
            System.out.println("  " + u);
        }
        separateur();
    }

    private void rechercherParPseudo() throws EsportException {
        System.out.println("\n  ── Recherche par pseudo ──");
        String pseudo = lireLigne("  Pseudo recherché : ");
        Utilisateur u = service.rechercherParPseudo(pseudo);
        System.out.println("\n" + u.sePresenter());
    }

    private void afficherFiche() throws EsportException {
        System.out.println("\n  ── Fiche utilisateur ──");
        int id = lireEntier("  ID utilisateur : ");
        Utilisateur u = service.rechercherParId(id);
        System.out.println("\n" + u.sePresenter());
    }
}
