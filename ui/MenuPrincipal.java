// ===== ui/MenuPrincipal.java =====
package ui;

import model.*;
import service.*;
import util.FichierUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu principal de la plateforme esport.
 * Orchestre tous les sous-menus et gère la persistance (sauvegarde / chargement).
 */
public class MenuPrincipal extends Menu {

    // ─── Chemins des fichiers de données ─────────────────────────────────────
    private static final String FICHIER_UTILISATEURS = "data/utilisateurs.txt";
    private static final String FICHIER_EQUIPES      = "data/equipes.txt";
    private static final String FICHIER_TOURNOIS     = "data/tournois.txt";
    private static final String FICHIER_MATCHS       = "data/matchs.txt";

    // ─── Services ─────────────────────────────────────────────────────────────
    private final GestionUtilisateur serviceUtilisateur;
    private final GestionEquipe      serviceEquipe;
    private final GestionTournoi     serviceTournoi;
    private final GestionMatch       serviceMatch;
    private final GestionPerformance servicePerformance;

    // ─── Sous-menus ───────────────────────────────────────────────────────────
    private final MenuUtilisateur menuUtilisateur;
    private final MenuEquipe      menuEquipe;
    private final MenuTournoi     menuTournoi;
    private final MenuMatch       menuMatch;
    private final MenuClassement  menuClassement;

    public MenuPrincipal(GestionUtilisateur gU, GestionEquipe gE, GestionTournoi gT,
                         GestionMatch gM, GestionPerformance gP, GestionClassement gC) {
        super(new Scanner(System.in));
        this.serviceUtilisateur = gU;
        this.serviceEquipe      = gE;
        this.serviceTournoi     = gT;
        this.serviceMatch       = gM;
        this.servicePerformance = gP;

        // Tous les sous-menus partagent le même Scanner
        this.menuUtilisateur = new MenuUtilisateur(scanner, gU);
        this.menuEquipe      = new MenuEquipe(scanner, gE, gU);
        this.menuTournoi     = new MenuTournoi(scanner, gT, gE);
        this.menuMatch       = new MenuMatch(scanner, gM, gT, gE, gP, gU);
        this.menuClassement  = new MenuClassement(scanner, gC, gU);
    }

    @Override
    public void afficher() {
        // Chargement automatique au démarrage
        System.out.println("\n  Chargement des données...");
        chargerDonnees();
        System.out.println("  Données chargées.");

        boolean continuer = true;
        while (continuer) {
            afficherBanniere();
            int choix = lireChoix();
            switch (choix) {
                case 1: menuUtilisateur.afficher();  break;
                case 2: menuEquipe.afficher();       break;
                case 3: menuTournoi.afficher();      break;
                case 4: menuMatch.afficher();        break;
                case 5: menuClassement.afficher();   break;
                case 6: sauvegarderDonnees();        pause(); break;
                case 7: chargerDonnees();            afficherSucces("Données rechargées."); pause(); break;
                case 0: continuer = false;           break;
                default: System.out.println("  Choix invalide."); pause(); break;
            }
        }

        // Sauvegarde automatique à la fermeture
        System.out.println("\n  Sauvegarde automatique...");
        sauvegarderDonnees();
        System.out.println("  Au revoir !");
        scanner.close();
    }

    // ─── Bannière ─────────────────────────────────────────────────────────────

    private void afficherBanniere() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      PLATEFORME ESPORT & TOURNOIS GAMING     ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  1. Gestion des utilisateurs                 ║");
        System.out.println("║  2. Gestion des équipes                      ║");
        System.out.println("║  3. Gestion des tournois                     ║");
        System.out.println("║  4. Gestion des matchs & performances        ║");
        System.out.println("║  5. Classements                              ║");
        System.out.println("║  ─────────────────────────────────────────── ║");
        System.out.println("║  6. Sauvegarder les données                  ║");
        System.out.println("║  7. Charger les données                      ║");
        System.out.println("║  0. Quitter                                  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // ─── Persistance ──────────────────────────────────────────────────────────

    /** Sauvegarde toutes les données dans les fichiers texte. */
    public void sauvegarderDonnees() {
        FichierUtil.sauvegarderUtilisateurs(serviceUtilisateur.afficherTous(), FICHIER_UTILISATEURS);
        FichierUtil.sauvegarderEquipes(serviceEquipe.afficherToutes(), FICHIER_EQUIPES);
        FichierUtil.sauvegarderTournois(serviceTournoi.afficherTous(), FICHIER_TOURNOIS);
        FichierUtil.sauvegarderMatchs(serviceMatch.afficherTous(), FICHIER_MATCHS);
        afficherSucces("Données sauvegardées dans le dossier 'data/'.");
    }

    /**
     * Charge toutes les données depuis les fichiers.
     * L'ordre est important : utilisateurs → équipes → tournois → matchs
     * car les équipes référencent des joueurs, etc.
     */
    public void chargerDonnees() {
        // 1. Réinitialise les services
        serviceUtilisateur.setUtilisateurs(new ArrayList<>());
        serviceEquipe.setEquipes(new ArrayList<>());
        serviceTournoi.setTournois(new ArrayList<>());
        serviceMatch.setMatchs(new ArrayList<>());
        servicePerformance.setPerformances(new ArrayList<>());

        // 2. Charge les utilisateurs
        List<Utilisateur> utilisateurs = FichierUtil.chargerUtilisateurs(FICHIER_UTILISATEURS);
        serviceUtilisateur.setUtilisateurs(new ArrayList<>(utilisateurs));

        // 3. Charge les équipes (reconstruction des références joueurs/coach)
        List<String[]> equipesRaw = FichierUtil.chargerEquipesRaw(FICHIER_EQUIPES);
        for (String[] parts : equipesRaw) {
            try {
                int    id         = Integer.parseInt(parts[0]);
                String nom        = parts[1];
                String jeu        = parts[2];
                int    nbVic      = Integer.parseInt(parts[3]);
                int    nbDef      = Integer.parseInt(parts[4]);
                String coachPseudo= parts[5];
                String capitaineId= parts[6];
                String joueurIds  = (parts.length > 7) ? parts[7] : "";

                Equipe equipe = new Equipe(id, nom, jeu, nbVic, nbDef);

                // Ajoute les joueurs par ID
                if (!joueurIds.isEmpty()) {
                    for (String jid : joueurIds.split(",")) {
                        try {
                            Utilisateur u = serviceUtilisateur.rechercherParId(Integer.parseInt(jid.trim()));
                            if (u instanceof Joueur) equipe.ajouterJoueur((Joueur) u);
                        } catch (Exception ignored) {}
                    }
                }
                // Affecte le coach par pseudo
                if (!coachPseudo.isEmpty()) {
                    try {
                        Utilisateur u = serviceUtilisateur.rechercherParPseudo(coachPseudo);
                        if (u instanceof Coach) equipe.affecterCoach((Coach) u);
                    } catch (Exception ignored) {}
                }
                // Définit le capitaine par ID
                if (!capitaineId.isEmpty()) {
                    try {
                        int capId = Integer.parseInt(capitaineId);
                        for (Joueur j : equipe.getJoueurs()) {
                            if (j.getId() == capId) { equipe.definirCapitaine(j); break; }
                        }
                    } catch (Exception ignored) {}
                }

                serviceEquipe.ajouter(equipe);
            } catch (Exception e) {
                System.err.println("[AVERTISSEMENT] Équipe ignorée : " + e.getMessage());
            }
        }

        // 4. Charge les tournois (reconstruction des références équipes)
        List<String[]> tournoisRaw = FichierUtil.chargerTournoisRaw(FICHIER_TOURNOIS);
        for (String[] parts : tournoisRaw) {
            try {
                int               id       = Integer.parseInt(parts[0]);
                String            nom      = parts[1];
                String            jeu      = parts[2];
                Tournoi.Format    format   = Tournoi.Format.valueOf(parts[3]);
                java.time.LocalDate debut   = java.time.LocalDate.parse(parts[4]);
                java.time.LocalDate fin     = java.time.LocalDate.parse(parts[5]);
                int               nbMax    = Integer.parseInt(parts[6]);
                Tournoi.Statut    statut   = Tournoi.Statut.valueOf(parts[7]);
                String            vainqId  = parts[8];
                String            equipeIds= (parts.length > 9) ? parts[9] : "";

                Tournoi tournoi = new Tournoi(id, nom, jeu, format, debut, fin, nbMax, statut);

                // Inscrit les équipes (bypass du statut car on charge)
                if (!equipeIds.isEmpty()) {
                    for (String eid : equipeIds.split(",")) {
                        try {
                            Equipe e = serviceEquipe.rechercherParId(Integer.parseInt(eid.trim()));
                            // Modification temporaire du statut pour permettre l'inscription
                            tournoi.setStatut(Tournoi.Statut.EN_ATTENTE);
                            tournoi.inscrireEquipe(e);
                            tournoi.setStatut(statut);
                        } catch (Exception ignored) {}
                    }
                }
                // Vainqueur
                if (!vainqId.isEmpty()) {
                    try {
                        Equipe v = serviceEquipe.rechercherParId(Integer.parseInt(vainqId));
                        tournoi.setVainqueur(v);
                    } catch (Exception ignored) {}
                }

                serviceTournoi.ajouter(tournoi);
            } catch (Exception e) {
                System.err.println("[AVERTISSEMENT] Tournoi ignoré : " + e.getMessage());
            }
        }

        // 5. Charge les matchs
        List<String[]> matchsRaw = FichierUtil.chargerMatchsRaw(FICHIER_MATCHS);
        for (String[] parts : matchsRaw) {
            try {
                int    id        = Integer.parseInt(parts[0]);
                Equipe e1        = serviceEquipe.rechercherParId(Integer.parseInt(parts[1]));
                Equipe e2        = serviceEquipe.rechercherParId(Integer.parseInt(parts[2]));
                int    score1    = Integer.parseInt(parts[3]);
                int    score2    = Integer.parseInt(parts[4]);
                String gagnantId = parts[5];
                LocalDateTime dh = LocalDateTime.parse(parts[6]);
                Match.Statut  st = Match.Statut.valueOf(parts[7]);
                String tNom      = parts[8];

                Match m = new Match(id, e1, e2, score1, score2, dh, st, tNom);
                if (!gagnantId.isEmpty()) {
                    int gId = Integer.parseInt(gagnantId);
                    if (gId == e1.getId())      m.setGagnant(e1);
                    else if (gId == e2.getId()) m.setGagnant(e2);
                }
                serviceMatch.ajouter(m);
            } catch (Exception e) {
                System.err.println("[AVERTISSEMENT] Match ignoré : " + e.getMessage());
            }
        }
    }
}
