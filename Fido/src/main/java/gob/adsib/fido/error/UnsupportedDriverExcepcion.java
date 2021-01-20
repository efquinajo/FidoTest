package gob.adsib.fido.error;

/**
 * @author ADSIB-UID
 */
public class UnsupportedDriverExcepcion extends Exception{
    public UnsupportedDriverExcepcion(Exception cause) {
        super("Controlador para dispositivo no compatile con el dispositivo",cause);
    }
}