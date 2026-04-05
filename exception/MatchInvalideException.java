// ===== exception/MatchInvalideException.java =====
package exception;

/**
 * Lancée quand les données d'un match sont invalides (scores négatifs, équipes identiques, etc.).
 */
public class MatchInvalideException extends EsportException {

    public MatchInvalideException(String message) {
        super(message);
    }
}
