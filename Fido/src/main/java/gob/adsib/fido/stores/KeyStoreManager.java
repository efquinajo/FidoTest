package gob.adsib.fido.stores;

import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.data.KeyAndCertificate;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * ADSIB-UID 20019
 * @author Ronald Coarite Mamani
 */
public abstract class KeyStoreManager extends BaseKeyStore{
    protected KeyStore keyStore;
    protected String pin;
    protected long slot;
    
    @Override
    public void login(String pin,long slot) throws Exception{
        this.pin = pin;
        this.slot = slot;
        loginKeyStore(pin, slot);
    }
    
    public abstract void loginKeyStore(String pin,long slot) throws Exception;

    @Override
    public KeyAndCertificate getKeyAndCertificate(String alias) throws Exception{       
        Object entry = keyStore.getEntry(alias,new KeyStore.PasswordProtection(pin.toCharArray()));
//        Object entry = keyStore.getEntry(alias,null);
        if(entry instanceof KeyStore.TrustedCertificateEntry){
            throw new IOException("El certificado NO posee la Clave Privada");
        }
        KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) entry;
        return new KeyAndCertificate(keyEntry.getPrivateKey(),(X509Certificate) keyEntry.getCertificate(), alias);
    }

    @Override
    public String updateCertificate(String alias,X509Certificate newX509Certificate)throws Exception{
        KeyAndCertificate keyAndCertificate = getKeyAndCertificate(alias);
        RSAPublicKey publicKeyLastCertificate = (RSAPublicKey)keyAndCertificate.getPublicKey();
        RSAPublicKey publicKeyNewCertificate = (RSAPublicKey)newX509Certificate.getPublicKey();
        
        String lastMod = publicKeyLastCertificate.getModulus().toString(16);
        String newMod = publicKeyNewCertificate.getModulus().toString(16);
        if(!lastMod.equals(newMod))
            throw new IOException("El certificado no corresponde al Par de Claves");
//        String newAlias = BaseKeyStore.generateRandomAlias();
        keyStore.setKeyEntry(alias,keyAndCertificate.getPrivateKey(), null,new Certificate[]{newX509Certificate});
//        keyStore.deleteEntry(alias);
        return alias;
    }
    
    @Override
    public List<String> getAlieces() throws Exception{
        Enumeration<String> enumeration = keyStore.aliases();
        ArrayList<String> list = new ArrayList<String>(3);
        while (enumeration.hasMoreElements()) {
            String param = enumeration.nextElement();
            list.add(param);
        }
        return list;
    }
    
    @Override
    public boolean isAuthenticated() {
        return keyStore!=null;
    }
    
    public abstract void save() throws IOException,KeyStoreException;
}