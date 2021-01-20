
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import java.io.File;

/**
 *
 * @author root
 */
public class Test2_CrearParClaves {
    public static void main(String cor[]) throws Exception{
        File fileProfile = new File("/root/Documentos/proyectos/jacobitus/source/Fido/profilePKCS#11_1569357368068.default.profile");

        Profile profile = Profile.readProfile(fileProfile);
        System.out.println(profile);
        BaseKeyStore keyStore = profile.getKeyStore();
        keyStore.login("12345678",0);
        keyStore.generateKeyAndCertificate("22222");
        keyStore.logout();
    }
}