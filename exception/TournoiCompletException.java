// ===== exception/TournoiCompletException.java =====
package exception;

/**
 * Lancée quand on tente d'inscrire une équipe dans un tournoi déjà plein.
 */
public class TournoiCompletException extends EsportException {

    public TournoiCompletException(String message) {
        super(message);
    }
}
