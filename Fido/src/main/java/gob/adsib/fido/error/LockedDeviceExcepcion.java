package gob.adsib.fido.error;

/**
 * @author ADSIB-UID
 */
public class LockedDeviceExcepcion extends Exception{
    public LockedDeviceExcepcion(Exception cause) {
        super("Dispositivo Token bloqueado por intentos fallidos al ingresar la Contraseña.",cause);
    }
}
