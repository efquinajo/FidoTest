package gob.adsib.fido.stores;

import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.error.DeviceNoConnected;
import gob.adsib.fido.error.GenericAuthenticationError;
import gob.adsib.fido.error.IncorrectPinException;
import gob.adsib.fido.error.LockedDeviceExcepcion;
import gob.adsib.fido.error.UnsupportedDriverExcepcion;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import gob.adsib.fido.util.IOUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * UID
 * @author Ronald Coarite Mamani
 */
public class PkcsN11Manager extends KeyStoreManager{
    private long slot;
    private SunPKCS11 sunPKCS11;
    private  File pathDriver;

    // CKF_SO_PIN_LOCKED
    public PkcsN11Manager(File pathDriver) {
        this.pathDriver = pathDriver;
    }
    
    @Override
    public long[] getSlots() throws Exception{
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        String functionList = "C_GetFunctionList";
        initArgs.flags = 0;
        System.out.println("DRIVER (getSlots): ["+pathDriver.getAbsolutePath()+"]");
        if(!pathDriver.isAbsolute())
            throw new IOException(String.format("La ruta para el controlador [%s] no es absoluta", pathDriver.getAbsolutePath()));
        if(!pathDriver.isFile())
            throw new IOException(String.format("La ruta para el controlador [%s] no es un archivo o no existe", pathDriver.getAbsolutePath()));
        PKCS11 pkcs11 = PKCS11.getInstance(pathDriver.getAbsolutePath(), functionList, initArgs, false);
        long[] slots = pkcs11.C_GetSlotList(true);

        finalizePKCS11Wrapper(pkcs11);
        return slots;
    }
    
    private void finalizePKCS11Wrapper(PKCS11 pkcs11) throws IOException{
        try {
            Field f = PKCS11.class.getDeclaredField("moduleMap"); 
            f.setAccessible(true);
            Map moduleMap = (Map) f.get(pkcs11);
            moduleMap.clear();
            pkcs11.C_Finalize(PKCS11Constants.NULL_PTR);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | PKCS11Exception e) {
            throw new IOException("No se pudo cerrar la sessión con el token",e);
        }
    }
    
    @Override
    public AbstractInfo getInfo(long slotToken) throws Exception{
        
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        
        String functionList = "C_GetFunctionList";
        initArgs.flags = 0;

        PKCS11 pkcs11 = PKCS11.getInstance(pathDriver.getAbsolutePath(), functionList, initArgs, false);
        try {
            CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slotToken);
            Info info = new Info();
            info.label = new String(tokenInfo.label).trim();
            info.model = new String(tokenInfo.model).trim();
            info.manufacture = new String(tokenInfo.manufacturerID).trim();
            info.serial = new String(tokenInfo.serialNumber).trim();
            info.maxPinLength = (int)tokenInfo.ulMaxPinLen;
            info.minPinLength = (int)tokenInfo.ulMinPinLen;
            info.suportOpenSC = false;
            return info;
        } catch (PKCS11Exception e) {
            if("CKR_GENERAL_ERROR".equals(e.getMessage())){
                throw new UnsupportedDriverExcepcion(e);
            }
            throw e;
        }
        finally{
            finalizePKCS11Wrapper(pkcs11);
        }
    }
    
    public class Info extends AbstractInfo{
        private String label;
        private String model;
        private String manufacture;
        private String serial;
        private int maxPinLength;
        private int minPinLength;
        private boolean suportOpenSC;
        
        private Info(){
            
        }

        @Override
        public String getGlobalName() {
            return getLabel();
        }

        @Override
        public String getSerialID() {
            return getSerial();
        }

        @Override
        public void writeData(JSONObject jsonResponse) {
            jsonResponse.put("label",getLabel());
            jsonResponse.put("model",getModel());
            jsonResponse.put("manufacture", getManufacture());
            jsonResponse.put("serial", getSerial());
            jsonResponse.put("max_pin_length",getMaxPinLength());
            jsonResponse.put("min_pin_length",getMinPinLength());
            jsonResponse.put("support_opensc",isSuportOpenSC());
        }

        public String getLabel() {
            return label;
        }

        public String getModel() {
            return model;
        }

        public String getManufacture() {
            return manufacture;
        }

        public String getSerial() {
            return serial;
        }

        public int getMaxPinLength() {
            return maxPinLength;
        }

        public int getMinPinLength() {
            return minPinLength;
        }

        public boolean isSuportOpenSC() {
            return suportOpenSC;
        }

        @Override
        public String toString() {
            return "Info{" + "label=" + label + ", model=" + model + ", manufacture=" + manufacture + ", serial=" + serial + ", maxPinLength=" + maxPinLength + ", minPinLength=" + minPinLength + ", suportOpenSC=" + suportOpenSC + '}';
        }
    }
    
    private void instanceMultipleJavaVersion(String configFilePath) throws GenericAuthenticationError{
        // Java 11
        try {
            if(IOUtil.getJavaVersion()>8){
                    Provider sunProvider = Security.getProvider("SunPKCS11");
                    Method method = Provider.class.getMethod("configure",String.class);
                    sunProvider = (Provider)method.invoke(sunProvider,configFilePath);
                    sunPKCS11 = (SunPKCS11)sunProvider;
//                    sunPKCS11 = (SunPKCS11)sunProvider.configure(configFilePath);
            }
            else{ 
                // Java 8
                Constructor constructor = SunPKCS11.class.getConstructor(String.class);
                sunPKCS11 = (SunPKCS11)constructor.newInstance(configFilePath);
    //            sunPKCS11 = new SunPKCS11(bais);
            }
        } catch (Exception e) {
            throw new GenericAuthenticationError("Error interno al iniciar proveedor de servicio para token",e);
        }
    }
    
    @Override
    public void loginKeyStore(String pin,long slot) throws IncorrectPinException,LockedDeviceExcepcion, GenericAuthenticationError, DeviceNoConnected, IOException {
        this.slot = slot;
        byte[] bytesConfig = getConfigPkcsN11();
        // Guardamos en un archivo temportal
        File filePkcs11Config = File.createTempFile("fido_pkcs11_",".cfg");
        // Compiamos la configuracion
        FileOutputStream fos = new FileOutputStream(filePkcs11Config);
        fos.write(bytesConfig);
        fos.flush();
        fos.close();
        
        
        System.out.println("RUTA CONFIGURACION: "+filePkcs11Config.getAbsolutePath());
        instanceMultipleJavaVersion(filePkcs11Config.getAbsolutePath());
        
        if(Security.getProperty(sunPKCS11.getName()) == null)
            Security.addProvider(sunPKCS11);
        
        try {
            keyStore =  KeyStore.getInstance("PKCS11", sunPKCS11);
            keyStore.load(null, pin.toCharArray());
        } catch (IOException e) {
            if(e.getCause() instanceof java.security.UnrecoverableKeyException){
                if(e.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException)
                    throw new IncorrectPinException(e);
            }
            if(e.getCause() instanceof javax.security.auth.login.LoginException){
                if(e.getCause().getCause() instanceof sun.security.pkcs11.wrapper.PKCS11Exception)
                    throw new LockedDeviceExcepcion(e);
            }
            throw new GenericAuthenticationError(e);
        }
        catch(KeyStoreException kse ){
            if(kse.getMessage().equals("PKCS11 not found"))
                throw new DeviceNoConnected(kse);
            else
                throw new GenericAuthenticationError(kse);
        }
        catch(NoSuchAlgorithmException | CertificateException e ){
            throw new GenericAuthenticationError(e);
        }
    }

    @Override
    public KeyAndCertificate generateKeyAndCertificate(String alias) throws Exception {
        // Generando par de claves
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA",sunPKCS11); 
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","BC"); 
        keyGen.initialize(2048);
        
        KeyPair keyPair = keyGen.generateKeyPair();
//        KeyPair keyPair = keyGen.genKeyPair();
        // Generando nombres
        X500NameBuilder issuerBuilder = new X500NameBuilder();
        issuerBuilder.addRDN(BCStyle.CN,"Certificado Temporal ADSIB-Generado por FIDO");
        
        X500NameBuilder subjectBuilder = new X500NameBuilder();
        subjectBuilder.addRDN(BCStyle.CN,"Certificado Auto Firmado-Reemplazar con Certificado Emitido");
        
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
      
        return new KeyAndCertificate(keyPair.getPrivate(), certificate, alias);
//        return new KeyAndCertificate(keyPair.getPrivate(), null, alias);
    }

    @Override
    public void logout() throws Exception {
        if(sunPKCS11==null)
            return;

        sunPKCS11.logout();

        try {
            // Obtenemos el Wrapper del sunPKCS11
            Field f = SunPKCS11.class.getDeclaredField("p11"); 
            f.setAccessible(true);
            PKCS11 objectPKCS11 = (PKCS11)f.get(sunPKCS11);
            finalizePKCS11Wrapper(objectPKCS11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        sunPKCS11.clear();
        sunPKCS11.setCallbackHandler(null);
        Security.removeProvider(sunPKCS11.getName());
        sunPKCS11 = null;
        keyStore = null;
        System.gc();
    }

    @Override
    public void removeKeyPair(String alias) throws Exception {
        System.out.println("Eliminando par de claves alias: "+alias);
        keyStore.deleteEntry(alias);
        keyStore.store(null);
    }
    
    private byte[] getConfigPkcsN11(){
        StringBuilder builder = new StringBuilder();
        //limpieza de caracteres especiales y espacios
//        String  nombreConfig = nombre.replaceAll("[^\\p{L}\\p{Z}]","").replace(" ", "");
//        
//        builder.append("name = ").append(nombreConfig);
//        if(descripcion!=null&&descripcion.length() == 0 )
//            builder.append("\n").append("description = ").append(descripcion);
        builder.append("name = temp");
        // Codificando la direcion de la libreria 
        builder.append("\n").append("library = ").append(pathDriver.getAbsolutePath());
        builder.append("\n").append("slot = ").append(slot);
//        builder.append("\n").append("slotListIndex=").append(slot);
        
        // Habilitamos la configuracion de token
        builder.append("\n");
        builder.append("attributes(*,*,*) = {\n");
        builder.append("CKA_TOKEN = true\n");
//        builder.append("CKF_SECONDARY_AUTHENTICATION = true\n");
        builder.append("}");
        
//        builder.append("\n").append("attributes(*,CKO_PRIVATE_KEY,*) = {");
////        builder.append("\n").append("CKA_NEVER_EXTRACTABLE=true");
//        builder.append("\n").append("CKA_EXTRACTABLE=false");
//        builder.append("\n").append("}");
        
//        builder.append("\n").append("attributes(generate,CKO_SECRET_KEY,CKK_GENERIC_SECRET,CKO_PRIVATE_KEY,*) = {\n" +
//        "  CKA_SENSITIVE = true\n" +
//        "  CKA_EXTRACTABLE = false\n" +
//        "}");

//        builder.append("\n").append("attributes(*,CKO_PRIVATE_KEY,*) = {");
//        builder.append("\n").append("CKA_ID = ").append("1111111111");
//        builder.append("\n").append("CKA_ID = ").append("1111111111");
//        builder.append("\n").append("CKA_SENSITIVE=true");
//        builder.append("\n").append("CKA_EXTRACTABLE=false");
//        builder.append("\n").append("}");
        
//        builder.append("\n").append("attributes(*, CKO_PRIVATE_KEY, *) = {\n" +
//"  CKA_ALWAYS_AUTHENTICATE = true\n" +
//"  CKA_SENSITIVE =  true\n" +
//"  CKA_EXTRACTABLE = false\n" +
//"  CKA_LOCAL = true\n" +
//"  CKA_ALWAYS_SENSITIVE = true\n" +
//"  CKA_ID = 33333\n" +
//"  CKA_LABEL = 0h707275656261\n" + 
//"  CKA_DECRYPT = true\n" +
//"  CKA_SIGN = true\n" +
//"  CKA_UNWRAP = true\n" +
//"  CKA_DERIVE = false\n" +
//"  CKA_NEVER_EXTRACTABLE = true\n" +
//"}\n");
        
        // Se agrega esta configuracion para dar soporte a Instancias RSAPrivateKey (Pruebas para Token Athena)
        builder.append("\n");
        builder.append("disabledMechanisms = {\n" +
        "     CKM_SHA1_RSA_PKCS\n" +
        "}");
        
        System.out.println("CONFIG");
        System.out.println(builder.toString());
        
        return builder.toString().getBytes();
    }

    @Override
    public void save() throws IOException,KeyStoreException{
    }
}
