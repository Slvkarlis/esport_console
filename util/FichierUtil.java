// ===== util/FichierUtil.java =====
package util;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilitaire de persistance en fichiers texte (format CSV personnalisé).
 * Utilise BufferedWriter / BufferedReader avec try-with-resources.
 *
 * Format CSV :
 *   Utilisateur  : role;id;pseudo;email;niveau;dateInscription;[champs spécifiques]
 *   Equipe       : id;nom;jeu;nbVictoires;nbDefaites;coachPseudo;capitaineId;joueurIds
 *   Tournoi      : id;nom;jeu;format;dateDebut;dateFin;nbEquipesMax;statut;vainqueurId;equipeIds
 *   Match        : id;equipe1Id;equipe2Id;score1;score2;gagnantId;dateHeure;statut;tournoiNom
 */
public class FichierUtil {

    private static final String SEPARATEUR = ";";
    private static final String SEPARATEUR_LISTE = ",";

    // ═══════════════════════════════════════════════════════════════
    // UTILISATEURS
    // ═══════════════════════════════════════════════════════════════

    public static void sauvegarderUtilisateurs(List<Utilisateur> utilisateurs, String fichier) {
        creerDossierParent(fichier);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            for (Utilisateur u : utilisateurs) {
                // Champs communs
                StringBuilder sb = new StringBuilder();
                sb.append(u.getRole()).append(SEPARATEUR)
                  .append(u.getId()).append(SEPARATEUR)
                  .append(u.getPseudo()).append(SEPARATEUR)
                  .append(u.getEmail()).append(SEPARATEUR)
                  .append(u.getNiveau()).append(SEPARATEUR)
                  .append(u.getDateInscription());

                // Champs spécifiques selon le type
                if (u instanceof Joueur) {
                    Joueur j = (Joueur) u;
                    sb.append(SEPARATEUR).append(j.getJeuPrincipal())
                      .append(SEPARATEUR).append(j.getRang())
                      .append(SEPARATEUR).append(j.getScoreTotal())
                      .append(SEPARATEUR).append(j.getNomEquipe());

                } else if (u instanceof Coach) {
                    Coach c = (Coach) u;
                    sb.append(SEPARATEUR).append(c.getSpecialite())
                      .append(SEPARATEUR).append(c.getAnneesExp())
                      .append(SEPARATEUR).append(c.getEquipeCoachee())
                      .append(SEPARATEUR).append(c.getNbVictoiresCoachees());

                } else if (u instanceof Administrateur) {
                    Administrateur a = (Administrateur) u;
                    sb.append(SEPARATEUR).append(a.getRoleAdmin())
                      .append(SEPARATEUR).append(String.join(SEPARATEUR_LISTE, a.getPermissions()))
                      .append(SEPARATEUR).append(a.getNbActionsEffectuees());
                }

                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Sauvegarde utilisateurs : " + e.getMessage());
        }
    }

    public static List<Utilisateur> chargerUtilisateurs(String fichier) {
        List<Utilisateur> liste = new ArrayList<>();
        File f = new File(fichier);
        if (!f.exists()) return liste;

        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (ligne.trim().isEmpty()) continue;
                String[] p = ligne.split(SEPARATEUR, -1);
                try {
                    String type            = p[0];
                    int    id              = Integer.parseInt(p[1]);
                    String pseudo          = p[2];
                    String email           = p[3];
                    String    niveau       = p[4];
                    LocalDate dateIns      = LocalDate.parse(p[5]);

                    Utilisateur u = null;
                    if ("Joueur".equals(type) && p.length >= 10) {
                        u = new Joueur(id, pseudo, email, niveau, dateIns,
                                p[6], p[7], Integer.parseInt(p[8]), p[9]);

                    } else if ("Coach".equals(type) && p.length >= 10) {
                        u = new Coach(id, pseudo, email, niveau, dateIns,
                                p[6], Integer.parseInt(p[7]), p[8], Integer.parseInt(p[9]));

                    } else if ("Administrateur".equals(type) && p.length >= 9) {
                        List<String> perms = new ArrayList<>();
                        if (!p[7].isEmpty()) {
                            perms = Arrays.asList(p[7].split(SEPARATEUR_LISTE));
                        }
                        u = new Administrateur(id, pseudo, email, niveau, dateIns,
                                p[6], perms, Integer.parseInt(p[8]));
                    }
                    if (u != null) liste.add(u);

                } catch (Exception ex) {
                    System.err.println("[AVERTISSEMENT] Ligne ignorée (utilisateurs) : " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Chargement utilisateurs : " + e.getMessage());
        }
        return liste;
    }

    // ═══════════════════════════════════════════════════════════════
    // ÉQUIPES  (les références aux joueurs/coach sont sauvegardées par ID/pseudo)
    // ═══════════════════════════════════════════════════════════════

    public static void sauvegarderEquipes(List<Equipe> equipes, String fichier) {
        creerDossierParent(fichier);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            for (Equipe e : equipes) {
                String coachPseudo   = (e.getCoach()      != null) ? e.getCoach().getPseudo()              : "";
                String capitaineId   = (e.getCapitaine()  != null) ? String.valueOf(e.getCapitaine().getId()) : "";

                // Liste des IDs des joueurs
                StringBuilder joueurIds = new StringBuilder();
                for (Joueur j : e.getJoueurs()) {
                    if (joueurIds.length() > 0) joueurIds.append(SEPARATEUR_LISTE);
                    joueurIds.append(j.getId());
                }

                bw.write(e.getId() + SEPARATEUR + e.getNom() + SEPARATEUR + e.getJeu()
                        + SEPARATEUR + e.getNbVictoires() + SEPARATEUR + e.getNbDefaites()
                        + SEPARATEUR + coachPseudo + SEPARATEUR + capitaineId
                        + SEPARATEUR + joueurIds);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Sauvegarde équipes : " + e.getMessage());
        }
    }

    /**
     * Retourne les données brutes (tableaux de chaînes) car la reconstruction
     * des objets Equipe (avec leurs Joueurs/Coach) nécessite les services.
     * La reconstruction est faite dans Main.
     */
    public static List<String[]> chargerEquipesRaw(String fichier) {
        return chargerLignesRaw(fichier);
    }

    // ═══════════════════════════════════════════════════════════════
    // TOURNOIS
    // ═══════════════════════════════════════════════════════════════

    public static void sauvegarderTournois(List<Tournoi> tournois, String fichier) {
        creerDossierParent(fichier);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            for (Tournoi t : tournois) {
                String vainqueurId = (t.getVainqueur() != null)
                        ? String.valueOf(t.getVainqueur().getId()) : "";

                StringBuilder equipeIds = new StringBuilder();
                for (Equipe e : t.getEquipes()) {
                    if (equipeIds.length() > 0) equipeIds.append(SEPARATEUR_LISTE);
                    equipeIds.append(e.getId());
                }

                bw.write(t.getId() + SEPARATEUR + t.getNom() + SEPARATEUR + t.getJeu()
                        + SEPARATEUR + t.getFormat() + SEPARATEUR + t.getDateDebut()
                        + SEPARATEUR + t.getDateFin() + SEPARATEUR + t.getNbEquipesMax()
                        + SEPARATEUR + t.getStatut() + SEPARATEUR + vainqueurId
                        + SEPARATEUR + equipeIds);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Sauvegarde tournois : " + e.getMessage());
        }
    }

    public static List<String[]> chargerTournoisRaw(String fichier) {
        return chargerLignesRaw(fichier);
    }

    // ═══════════════════════════════════════════════════════════════
    // MATCHS
    // ═══════════════════════════════════════════════════════════════

    public static void sauvegarderMatchs(List<Match> matchs, String fichier) {
        creerDossierParent(fichier);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            for (Match m : matchs) {
                String gagnantId = (m.getGagnant() != null)
                        ? String.valueOf(m.getGagnant().getId()) : "";

                bw.write(m.getId() + SEPARATEUR
                        + m.getEquipe1().getId() + SEPARATEUR
                        + m.getEquipe2().getId() + SEPARATEUR
                        + m.getScoreEquipe1() + SEPARATEUR
                        + m.getScoreEquipe2() + SEPARATEUR
                        + gagnantId + SEPARATEUR
                        + m.getDateHeure() + SEPARATEUR
                        + m.getStatut() + SEPARATEUR
                        + m.getTournoiNom());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Sauvegarde matchs : " + e.getMessage());
        }
    }

    public static List<String[]> chargerMatchsRaw(String fichier) {
        return chargerLignesRaw(fichier);
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers privés
    // ═══════════════════════════════════════════════════════════════

    /** Lit un fichier CSV et retourne chaque ligne comme tableau de chaînes. */
    private static List<String[]> chargerLignesRaw(String fichier) {
        List<String[]> liste = new ArrayList<>();
        File f = new File(fichier);
        if (!f.exists()) return liste;

        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (!ligne.trim().isEmpty()) {
                    liste.add(ligne.split(SEPARATEUR, -1));
                }
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Lecture fichier '" + fichier + "' : " + e.getMessage());
        }
        return liste;
    }

    /** Crée le dossier parent du fichier s'il n'existe pas. */
    private static void creerDossierParent(String fichier) {
        File parent = new File(fichier).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
