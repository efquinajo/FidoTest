
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import java.io.File;
import java.util.List;

/**
 *
 * @author root
 */
public class Test4_GenerarCsr {
    public static void main(String cor[]) throws Exception{
        File fileProfile = new File("profile_temp2.properties");

        Profile profile = Profile.readProfile(fileProfile);
        System.out.println(profile);
        BaseKeyStore keyStore = profile.getKeyStore();
        keyStore.login("Adsib2025",0);
        System.out.println("LISTA DE ALIAS");
        List<String> listAlias = keyStore.getAlieces();
        for (String alias: listAlias) {
            System.out.println("\t"+alias);
        }
//        keyStore.generateKeyAndCertificate("prueba");
        keyStore.logout();
    }
}