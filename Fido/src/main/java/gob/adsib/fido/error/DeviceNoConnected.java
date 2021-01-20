package gob.adsib.fido.error;

/**
 * @author ADSIB-UID
 */
public class DeviceNoConnected extends Exception{
    public DeviceNoConnected(Exception cause) {
        super("Dispositivo token no conectado",cause);
    }
}