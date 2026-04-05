// ===== exception/EquipeIntrouvableException.java =====
package exception;

/**
 * Lancée quand une équipe recherchée par ID ou par nom est introuvable.
 */
public class EquipeIntrouvableException extends EsportException {

    public EquipeIntrouvableException(String message) {
        super(message);
    }
}
