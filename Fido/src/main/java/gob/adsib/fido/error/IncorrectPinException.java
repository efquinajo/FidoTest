package gob.adsib.fido.error;

/**
 * @author ADSIB-UID
 */
public class IncorrectPinException extends Exception{
    public IncorrectPinException(Exception cause) {
        super("La Contraseña es incorrecto.",cause);
    }
}
