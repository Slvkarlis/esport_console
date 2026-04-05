// ===== ui/Menu.java =====
package ui;

import java.util.Scanner;

/**
 * Classe de base abstraite pour tous les menus de la console.
 * Fournit les méthodes utilitaires de saisie.
 */
public abstract class Menu {

    protected final Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Affiche le menu et traite les interactions utilisateur. */
    public abstract void afficher();

    // ─── Utilitaires de saisie ────────────────────────────────────────────────

    /** Lit un entier depuis stdin. Redemande tant que l'entrée n'est pas un entier. */
    protected int lireChoix() {
        System.out.print("  → Votre choix : ");
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("  Entrez un nombre entier : ");
        }
        int choix = scanner.nextInt();
        scanner.nextLine(); // consomme le '\n' résiduel
        return choix;
    }

    /** Lit une ligne de texte avec un prompt. */
    protected String lireLigne(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Lit un entier avec un prompt. */
    protected int lireEntier(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("  Entrez un entier valide : ");
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    /** Lit un double avec un prompt. */
    protected double lireDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            scanner.next();
            System.out.print("  Entrez un nombre décimal valide : ");
        }
        double val = scanner.nextDouble();
        scanner.nextLine();
        return val;
    }

    /** Affiche un message d'erreur formaté. */
    protected void afficherErreur(String message) {
        System.out.println("\n  [ERREUR] " + message);
    }

    /** Affiche un message de succès. */
    protected void afficherSucces(String message) {
        System.out.println("\n  [OK] " + message);
    }

    /** Attend que l'utilisateur appuie sur Entrée. */
    protected void pause() {
        System.out.print("\n  Appuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    /** Affiche une ligne de séparation. */
    protected void separateur() {
        System.out.println("  ─────────────────────────────────────────");
    }
}
