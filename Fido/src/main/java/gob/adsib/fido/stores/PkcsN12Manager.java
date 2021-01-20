package gob.adsib.fido.stores;

import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * UID
 * @author Ronald Coarite Mamani
 */
public class PkcsN12Manager extends KeyStoreManager{
    private final File fileP12;
    private String pin;
    
    public PkcsN12Manager(File fileP12) {
        this.fileP12 = fileP12;
    }
    
    public void createNewKeyStoreFile(String pin) throws IOException, KeyStoreException, Exception{
        this.pin = pin;
        fileP12.createNewFile();
        this.keyStore = KeyStore.getInstance("PKCS12");
        this.keyStore.load(null, null);
        save();
        login(pin, 0);
    }
    
    public void addOrUpdateKeyAndCertificate(KeyAndCertificate keyAndCertificate) throws KeyStoreException, IOException{
        this.keyStore.setKeyEntry(keyAndCertificate.getAlias(), keyAndCertificate.getPrivateKey(), null,new Certificate[]{keyAndCertificate.getX509Certificate()});
        save();
    }

    @Override
    public long[] getSlots() throws Exception{
        return new long[]{0};
    }

    @Override
    public KeyAndCertificate generateKeyAndCertificate(String alias) throws Exception{
        // Generando par de claves
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","BC"); 
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        // Generando nombres
        X500NameBuilder issuerBuilder = new X500NameBuilder();
        issuerBuilder.addRDN(BCStyle.CN,"PADRE");
        
        X500NameBuilder subjectBuilder = new X500NameBuilder();
        subjectBuilder.addRDN(BCStyle.CN,"HIJO");
        
        // Generando certificado autofirmado
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuerBuilder.build(), // Nombre comun emisor
                new BigInteger("111111"),  // Nro. serial certificado
                new Date(), // 
                new Date(), 
                subjectBuilder.build(), // Nombre comun titular
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = builder.build(keyPair.getPrivate());

        byte[] certBytes = certBuilder.build(signer).getEncoded();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), null,new Certificate[]{certificate});
        
        save();
        return new KeyAndCertificate(keyPair.getPrivate(), certificate, alias);
    }

    @Override
    public AbstractInfo getInfo(long slot) throws Exception{
        return new AbstractInfo() {
            @Override
            public String getGlobalName() {
                return "Archivo .p12";
            }

            @Override
            public String getSerialID() {
                return "0";
            }
        };
    }
    
    @Override
    public void loginKeyStore(String pin,long slot) throws Exception {
        this.pin = pin;
        keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new FileInputStream(fileP12), pin.toCharArray());
    }

    @Override
    public void logout() throws Exception {
        keyStore = null;
        System.gc();
    }

    @Override
    public void save() throws IOException,KeyStoreException{
        try {
            keyStore.store(new FileOutputStream(fileP12),pin.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeKeyPair(String alias) throws Exception {
        keyStore.deleteEntry(alias);
        save();
    }
}