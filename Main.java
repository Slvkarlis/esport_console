// ===== Main.java =====

import service.*;
import ui.MenuPrincipal;

/**
 * Point d'entrée de la plateforme esport & tournois gaming.
 *
 * Compilation (depuis le dossier esport_v2) :
 *   javac -encoding UTF-8 -d out model/*.java exception/*.java service/*.java util/*.java ui/*.java Main.java
 *
 * Exécution :
 *   java -cp out Main
 */
public class Main {

    public static void main(String[] args) {
        // ─── Initialisation des services ──────────────────────────────────────
        GestionUtilisateur gestionUtilisateur = new GestionUtilisateur();
        GestionEquipe      gestionEquipe      = new GestionEquipe();
        GestionTournoi     gestionTournoi     = new GestionTournoi();
        GestionMatch       gestionMatch       = new GestionMatch();
        GestionPerformance gestionPerformance = new GestionPerformance();

        // GestionClassement agrège les autres services (injection de dépendances)
        GestionClassement gestionClassement = new GestionClassement(
                gestionUtilisateur, gestionEquipe, gestionPerformance);

        // Mode API locale : java -cp out Main api
        if (args != null && args.length > 0 && "api".equalsIgnoreCase(args[0])) {
            int apiPort = 8081;
            if (args.length > 1) {
                try {
                    apiPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    System.out.println("[API] Port invalide, utilisation du port 8081.");
                }
            }

            BackendDataStore dataStore = new BackendDataStore();
            dataStore.loadAll(
                gestionUtilisateur,
                gestionEquipe,
                gestionTournoi,
                gestionMatch,
                gestionPerformance
            );

            ApiServer apiServer = new ApiServer(
                gestionUtilisateur,
                gestionEquipe,
                gestionTournoi,
                gestionMatch,
                gestionPerformance,
                dataStore
            );

            try {
                apiServer.start(apiPort);
            System.out.println("[API] Appuyez sur Ctrl+C pour arrêter le serveur.");
            } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de démarrer l'API: " + e.getMessage());
            }
            return;
        }

        // ─── Lancement du menu principal ──────────────────────────────────────
        // Le menu se charge lui-même des données au démarrage
        // et sauvegarde à la fermeture.
        MenuPrincipal menu = new MenuPrincipal(
                gestionUtilisateur,
                gestionEquipe,
                gestionTournoi,
                gestionMatch,
                gestionPerformance,
                gestionClassement);

        menu.afficher();
    }
}
