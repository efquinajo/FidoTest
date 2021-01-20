package gob.adsib.fido.error;

/**
 * @author ADSIB-UID
 */
public class GenericAuthenticationError extends Exception{
    public GenericAuthenticationError(Exception cause) {
        super("Error desconocido para la autenticación.",cause);
    }
    public GenericAuthenticationError(String message,Exception cause) {
        super(message,cause);
    }
}