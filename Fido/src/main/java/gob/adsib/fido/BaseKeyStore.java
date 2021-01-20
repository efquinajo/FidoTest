package gob.adsib.fido;



import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 *
 * @author UID-ADSIB
 */
public abstract class BaseKeyStore {
    public abstract AbstractInfo getInfo(long slot) throws Exception;
    
    public abstract void login(String pin,long slot) throws Exception;
    public abstract void logout() throws Exception;
    public abstract long[] getSlots() throws Exception;
    public abstract boolean isAuthenticated();
    public abstract KeyAndCertificate getKeyAndCertificate(String alias) throws Exception;
    public abstract String updateCertificate(String alias,X509Certificate x509Certificate) throws Exception;
    public abstract KeyAndCertificate generateKeyAndCertificate(String alias) throws Exception;
    public abstract List<String> getAlieces()  throws Exception;
    public static String generateRandomAlias(){
        String randomAlias = String.valueOf((long)Double.parseDouble(""+Math.random()*100000000000000L));
        return randomAlias;
    }

    public abstract void removeKeyPair(String alias) throws Exception;
}