package gob.adsib.fido;

import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.filechooser.FileSystemView;

/**s
 *
 * @author root, refactor (Jhonny Monrroy)
 */
public class Config {
    private static Config config;
    private final static String CONFIG_FILE_NAME = "application.properties";
    private static String PROPERTIE_APP_NAME = "fido_app.properties";
    public static int DEFAULT_SLOT= 0;
    public static String PROFILE_DIR_NAME = "FidoProfiles";
    
    private Properties props = null;

    private Config(){
        
    }
    
    public static Properties getPropertieUser(){
        Properties properties = new Properties();
        try {
            File folderProfile = Config.getFolderProfile();
            if(!folderProfile.exists())
                folderProfile.mkdirs();
            File filePropertie = new File(folderProfile, Config.PROPERTIE_APP_NAME);
            if(!filePropertie.exists()){
                properties.put("brouserCertificateAdded","false");
                properties.store(new FileOutputStream(filePropertie), "Archivo de configuracion de FIDO");
            }
            properties.load(new FileInputStream(filePropertie));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
    
    public static File getFolderProfile(){
        File folder = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()+File.separator+Config.PROFILE_DIR_NAME);
        return folder;
    }
    
    public static Config getConfig(){
        if(config == null){
            config = new Config();
            try {
                config.props = new Properties();
                System.out.println("Leendo parametros de: "+new File(CONFIG_FILE_NAME).getAbsolutePath());
                InputStream input = new FileInputStream(new File(CONFIG_FILE_NAME));
                config.props.load(input);
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }
    
    public static Config reLoad(){
        config = null;
        return getConfig();
    }
    
    private String getProperty(String propertiKey){
        String value = props.getProperty(propertiKey);
        if(value == null)
            return null;
        if(value.length()==0)
            return null;
        return value;
    }
    
    public String getCurrentProfileName(){
        String profileName = getProperty("profile.name");
        return profileName;
    }
    
    public int getPortFido(){
        return Integer.parseInt(getProperty("fido.puerto"));
    }
    
    public boolean isEnabledFirmaticServiceOnFido(){
        Properties properties = getPropertieUser();
        return Boolean.parseBoolean(properties.getProperty("firmatic.enabled","false"));
    }
    
    public void setEnabledFirmaticService(boolean enabled) throws FileNotFoundException, IOException{
        Properties properties = getPropertieUser();
        properties.setProperty("firmatic.enabled",String.valueOf(enabled));
        File folderProfile = Config.getFolderProfile();
        File filePropertie = new File(folderProfile, Config.PROPERTIE_APP_NAME);
        properties.store(new FileOutputStream(filePropertie), "Archivo de configuracion de FIDO");
    }

    public String getPathCertificadoEmisor(){
        return getProperty("certificado.emisor");
    }

    public String isEnabledOpenScDriver(){
        return getProperty("opensc.driver_enabled");
    }

    public String getProxyIp() {
        return getProperty("proxy.ip");
    }

    public Integer getProxyPort() {
        return Integer.parseInt(getProperty("proxy.port"));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("------ APPLICATION.PROPERTIES -----").append("\n");
        builder.append(props.toString());
        builder.append("------ EN APPICATION.PROPERTIES -----");
        return builder.toString();
    }
    
    public void save() throws FileNotFoundException, IOException{
        System.out.println("Guardando cambios en archivo: "+new File(CONFIG_FILE_NAME).getAbsolutePath());
        System.out.println(toString());
        props.store(new FileOutputStream(CONFIG_FILE_NAME),"Archivo de configuración");
    }
    
    public static void validarDriverOpenSC(Profile profile) throws IOException{
        if("false".equals(Config.getConfig().isEnabledOpenScDriver())){
            final String mensajeDriverNoCompatible = "El software de su token debe ser actualizado, favor de comunicarse con soporte técnico de ADSIB.";
            if(profile instanceof Pkcs11Profile){
                Pkcs11Profile pkcs11Profile = (Pkcs11Profile)profile;
                File fileDriver = pkcs11Profile.getPathDriver();
                if(fileDriver.getName().lastIndexOf("opensc-pkcs11.dll")!=-1)
                    throw new IOException(mensajeDriverNoCompatible);
                if(fileDriver.getName().lastIndexOf("opensc-pkcs11.so")!=-1)
                    throw new IOException(mensajeDriverNoCompatible);
            }
        }
    }
}