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
