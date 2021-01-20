
import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import java.io.File;

/**
 *
 * @author root
 */
public class Test1_CrearPerfil {
    public static void main(String cor[]) throws Exception{
        File driverPath = new File("/usr/lib/libeTPkcs11.so");
        int slot = 0;
        String nombrePerfile = "Prueba perfil";
        File fileProfile = new File("profile_temp2.properties");
        Pkcs11Profile pkcs11Profile = Pkcs11Profile.newInstance(fileProfile,nombrePerfile,driverPath, slot);
        pkcs11Profile.save();
    }
}