
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author GIGABYTE
 */
public class Test_10_FirmaJose {
    public static void main(String cor[]) throws IOException, Exception{
        
        Security.addProvider(new BouncyCastleProvider());
        
        File fileProfile = new File("C:\\Users\\GIGABYTE\\Documents\\FidoProfiles\\1593740352606.default.profile");
        String alias = "72609233121856";

        Profile profile = Profile.readProfile(fileProfile);
        System.out.println(profile);
        BaseKeyStore keyStore = profile.getKeyStore();
        keyStore.login("12345678",1);
        KeyAndCertificate keyAndCertificate = keyStore.getKeyAndCertificate(alias);
        
        String serialiceJWS = keyAndCertificate.signJsonText("EL VALOR DEL CSR","https://certificado.bo");
        System.out.println(serialiceJWS);
        keyStore.logout();
    }
}
