import model.Administrateur;
import model.Coach;
import model.Equipe;
import model.Joueur;
import model.Match;
import model.Tournoi;
import model.Utilisateur;
import service.GestionEquipe;
import service.GestionMatch;
import service.GestionPerformance;
import service.GestionTournoi;
import service.GestionUtilisateur;
import util.FichierUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistance fichier pour le mode API.
 * Réutilise le même format que le menu console (dossier data/).
 */
public class BackendDataStore {

    private static final String FICHIER_UTILISATEURS = "data/utilisateurs.txt";
    private static final String FICHIER_EQUIPES = "data/equipes.txt";
    private static final String FICHIER_TOURNOIS = "data/tournois.txt";
    private static final String FICHIER_MATCHS = "data/matchs.txt";

    public void loadAll(
            GestionUtilisateur serviceUtilisateur,
            GestionEquipe serviceEquipe,
            GestionTournoi serviceTournoi,
            GestionMatch serviceMatch,
            GestionPerformance servicePerformance
    ) {
        serviceUtilisateur.setUtilisateurs(new ArrayList<>());
        serviceEquipe.setEquipes(new ArrayList<>());
        serviceTournoi.setTournois(new ArrayList<>());
        serviceMatch.setMatchs(new ArrayList<>());
        servicePerformance.setPerformances(new ArrayList<>());

        List<Utilisateur> utilisateurs = FichierUtil.chargerUtilisateurs(FICHIER_UTILISATEURS);
        serviceUtilisateur.setUtilisateurs(new ArrayList<>(utilisateurs));

        List<String[]> equipesRaw = FichierUtil.chargerEquipesRaw(FICHIER_EQUIPES);
        for (String[] parts : equipesRaw) {
            try {
                int id = Integer.parseInt(parts[0]);
                String nom = parts[1];
                String jeu = parts[2];
                int nbVic = Integer.parseInt(parts[3]);
                int nbDef = Integer.parseInt(parts[4]);
                String coachPseudo = parts[5];
                String capitaineId = parts[6];
                String joueurIds = (parts.length > 7) ? parts[7] : "";

                Equipe equipe = new Equipe(id, nom, jeu, nbVic, nbDef);

                if (!joueurIds.isEmpty()) {
                    for (String jid : joueurIds.split(",")) {
                        try {
                            Utilisateur u = serviceUtilisateur.rechercherParId(Integer.parseInt(jid.trim()));
                            if (u instanceof Joueur) {
                                equipe.ajouterJoueur((Joueur) u);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (!coachPseudo.isEmpty()) {
                    try {
                        Utilisateur u = serviceUtilisateur.rechercherParPseudo(coachPseudo);
                        if (u instanceof Coach) {
                            equipe.affecterCoach((Coach) u);
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (!capitaineId.isEmpty()) {
                    try {
                        int capId = Integer.parseInt(capitaineId);
                        for (Joueur j : equipe.getJoueurs()) {
                            if (j.getId() == capId) {
                                equipe.definirCapitaine(j);
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                serviceEquipe.ajouter(equipe);
            } catch (Exception e) {
                System.err.println("[WARN] Equipe ignoree: " + e.getMessage());
            }
        }

        List<String[]> tournoisRaw = FichierUtil.chargerTournoisRaw(FICHIER_TOURNOIS);
        for (String[] parts : tournoisRaw) {
            try {
                int id = Integer.parseInt(parts[0]);
                String nom = parts[1];
                String jeu = parts[2];
                Tournoi.Format format = Tournoi.Format.valueOf(parts[3]);
                LocalDate debut = LocalDate.parse(parts[4]);
                LocalDate fin = LocalDate.parse(parts[5]);
                int nbMax = Integer.parseInt(parts[6]);
                Tournoi.Statut statut = Tournoi.Statut.valueOf(parts[7]);
                String vainqId = parts[8];
                String equipeIds = (parts.length > 9) ? parts[9] : "";

                Tournoi tournoi = new Tournoi(id, nom, jeu, format, debut, fin, nbMax, statut);

                if (!equipeIds.isEmpty()) {
                    for (String eid : equipeIds.split(",")) {
                        try {
                            Equipe e = serviceEquipe.rechercherParId(Integer.parseInt(eid.trim()));
                            tournoi.setStatut(Tournoi.Statut.EN_ATTENTE);
                            tournoi.inscrireEquipe(e);
                            tournoi.setStatut(statut);
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (!vainqId.isEmpty()) {
                    try {
                        Equipe v = serviceEquipe.rechercherParId(Integer.parseInt(vainqId));
                        tournoi.setVainqueur(v);
                    } catch (Exception ignored) {
                    }
                }

                serviceTournoi.ajouter(tournoi);
            } catch (Exception e) {
                System.err.println("[WARN] Tournoi ignore: " + e.getMessage());
            }
        }

        List<String[]> matchsRaw = FichierUtil.chargerMatchsRaw(FICHIER_MATCHS);
        for (String[] parts : matchsRaw) {
            try {
                int id = Integer.parseInt(parts[0]);
                Equipe e1 = serviceEquipe.rechercherParId(Integer.parseInt(parts[1]));
                Equipe e2 = serviceEquipe.rechercherParId(Integer.parseInt(parts[2]));
                int score1 = Integer.parseInt(parts[3]);
                int score2 = Integer.parseInt(parts[4]);
                String gagnantId = parts[5];
                LocalDateTime dh = LocalDateTime.parse(parts[6]);
                Match.Statut st = Match.Statut.valueOf(parts[7]);
                String tNom = parts[8];

                Match m = new Match(id, e1, e2, score1, score2, dh, st, tNom);
                if (!gagnantId.isEmpty()) {
                    int gId = Integer.parseInt(gagnantId);
                    if (gId == e1.getId()) {
                        m.setGagnant(e1);
                    } else if (gId == e2.getId()) {
                        m.setGagnant(e2);
                    }
                }
                serviceMatch.ajouter(m);
            } catch (Exception e) {
                System.err.println("[WARN] Match ignore: " + e.getMessage());
            }
        }

        seedIfEmpty(serviceUtilisateur, serviceEquipe, serviceTournoi, serviceMatch);
    }

    public void saveAll(
            GestionUtilisateur serviceUtilisateur,
            GestionEquipe serviceEquipe,
            GestionTournoi serviceTournoi,
            GestionMatch serviceMatch
    ) {
        FichierUtil.sauvegarderUtilisateurs(serviceUtilisateur.afficherTous(), FICHIER_UTILISATEURS);
        FichierUtil.sauvegarderEquipes(serviceEquipe.afficherToutes(), FICHIER_EQUIPES);
        FichierUtil.sauvegarderTournois(serviceTournoi.afficherTous(), FICHIER_TOURNOIS);
        FichierUtil.sauvegarderMatchs(serviceMatch.afficherTous(), FICHIER_MATCHS);
    }

    private void seedIfEmpty(
            GestionUtilisateur serviceUtilisateur,
            GestionEquipe serviceEquipe,
            GestionTournoi serviceTournoi,
            GestionMatch serviceMatch
    ) {
        if (!serviceUtilisateur.afficherTous().isEmpty()
                || !serviceEquipe.afficherToutes().isEmpty()
                || !serviceTournoi.afficherTous().isEmpty()
                || !serviceMatch.afficherTous().isEmpty()) {
            return;
        }

        try {
            Joueur zeroRaid = new Joueur("ZeroRaid", "zero@sz.gg", "87", "VALORANT");
            Joueur phantomX = new Joueur("PhantomX", "phantom@sz.gg", "82", "LOL");
            Joueur skyKnight = new Joueur("SkyKnight", "sky@sz.gg", "78", "CSGO");
            Joueur darkFlare = new Joueur("DarkFlare", "dark@sz.gg", "76", "VALORANT");
            Coach coachMike = new Coach("CoachMike", "mike@sz.gg", "95", "Strategie", 5);
            Coach coachAna = new Coach("CoachAna", "ana@sz.gg", "90", "Macro", 4);
            Administrateur admin = new Administrateur("AdminRoot", "admin@sz.gg", "99", "PLATFORM_ADMIN");

            serviceUtilisateur.ajouter(zeroRaid);
            serviceUtilisateur.ajouter(phantomX);
            serviceUtilisateur.ajouter(skyKnight);
            serviceUtilisateur.ajouter(darkFlare);
            serviceUtilisateur.ajouter(coachMike);
            serviceUtilisateur.ajouter(coachAna);
            serviceUtilisateur.ajouter(admin);

            Equipe nova = new Equipe("NOVA", "VALORANT");
            Equipe nexa = new Equipe("VOID", "LOL");

            serviceEquipe.ajouter(nova);
            serviceEquipe.ajouter(nexa);

            serviceEquipe.ajouterJoueurAEquipe(nova.getId(), zeroRaid);
            serviceEquipe.ajouterJoueurAEquipe(nova.getId(), skyKnight);
            serviceEquipe.ajouterJoueurAEquipe(nexa.getId(), phantomX);
            serviceEquipe.ajouterJoueurAEquipe(nexa.getId(), darkFlare);

            serviceEquipe.affecterCoach(nova.getId(), coachMike);
            serviceEquipe.affecterCoach(nexa.getId(), coachAna);
            serviceEquipe.definirCapitaine(nova.getId(), zeroRaid);
            serviceEquipe.definirCapitaine(nexa.getId(), phantomX);

            Tournoi springCup = new Tournoi(
                    "Spring Cup",
                    "VALORANT",
                    Tournoi.Format.ROUND_ROBIN,
                    LocalDate.now(),
                    LocalDate.now().plusDays(2),
                    8
            );
            serviceTournoi.ajouter(springCup);
            serviceTournoi.inscrireEquipe(springCup.getId(), nova);
            serviceTournoi.inscrireEquipe(springCup.getId(), nexa);

            Match match = new Match(nova, nexa, LocalDateTime.now().plusHours(1), springCup.getNom());
            serviceMatch.ajouter(match);
            springCup.ajouterMatch(match);

            saveAll(serviceUtilisateur, serviceEquipe, serviceTournoi, serviceMatch);
        } catch (Exception e) {
            System.err.println("[WARN] Seed data partiel: " + e.getMessage());
        }
    }
}
