// ===== exception/JoueurDejaInscritException.java =====
package exception;

/**
 * Lancée quand on tente d'ajouter un joueur déjà présent dans une équipe ou un service.
 */
public class JoueurDejaInscritException extends EsportException {

    public JoueurDejaInscritException(String message) {
        super(message);
    }
}
