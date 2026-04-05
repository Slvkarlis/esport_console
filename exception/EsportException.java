// ===== exception/EsportException.java =====
package exception;

/**
 * Exception de base pour toutes les erreurs métier de la plateforme esport.
 * Toutes les exceptions spécifiques héritent de cette classe.
 */
public class EsportException extends Exception {

    public EsportException(String message) {
        super(message);
    }

    public EsportException(String message, Throwable cause) {
        super(message, cause);
    }
}
