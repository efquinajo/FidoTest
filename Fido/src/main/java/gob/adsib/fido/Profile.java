package gob.adsib.fido;

import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import gob.adsib.fido.stores.profiles.Pkcs12Profile;
import gob.adsib.fido.util.os.AbstractOs;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import net.minidev.json.JSONObject;

/**
 * UID-ADSIB
 * @author Ronald Coarite Mamani
 */
public abstract class Profile {
    protected final Properties properties;
    protected final File file;
    public static final String TYPE_PKCS12 = "PKCS12";
    public static final String TYPE_PKCS11 = "PKCS11";
    
    protected Profile(File fileProfile) throws FileNotFoundException, IOException{
        this.file = fileProfile;
        properties = new Properties();
        FileInputStream fis = new FileInputStream(fileProfile);
        properties.load(fis);
        if(properties.containsKey("type")){
           properties.setProperty("type",properties.getProperty("type").replace("#",""));
        }
            
        fis.close();
    }
    
    public static File getFolderProfile(){
        File folder = new File(AbstractOs.getUserDirHome()+File.separator+Config.PROFILE_DIR_NAME);
        return folder;
    }
    
    public static File createFileProfile(String type){
        List<Profile>  listProfile = getListProfile();
        File fileProfile = null;
        if(listProfile.isEmpty())
            fileProfile = new File(getFolderProfile().getAbsolutePath()+File.separator+new Date().getTime()+".default.profile");
        else
            fileProfile = new File(getFolderProfile().getAbsolutePath()+File.separator+new Date().getTime()+".profile");
        System.out.println("CREANDO PERFIL EN: "+fileProfile.getAbsolutePath());
        return fileProfile;
    }
            
    public static List<Profile> getListProfile(){
        File folder = getFolderProfile();
        System.out.println("RUTA PERFILES: "+folder.getAbsolutePath());
        folder = new File(folder.getAbsolutePath());
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().contains(".profile");
            }
        });
        
        LinkedList<Profile> list = new LinkedList<>();
        for (File fileProfile : files) {
            try {
                Profile profile = Profile.readProfile(fileProfile);
                list.add(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    
    public static Profile getDefaultProfile() throws IOException{
        // Buscamos todos los perfiles creados con .profile
        List<Profile> listProfile = getListProfile();
        for (Profile profile : listProfile) {
            if(profile.isDefaultProfile())
                return profile;
        }
        if(listProfile.isEmpty())
            return null;
        return listProfile.get(0);
    }
    
    public abstract void writeDate(JSONObject jsonObject);
    
    protected Profile(File fileOutput,String type,String name){
        file = fileOutput;
        properties = new Properties();
        properties.setProperty("type", type);
        properties.setProperty("name", name);
    }
    
    public static Profile readProfile(File fileProfile) throws FileNotFoundException, IOException{
        Properties propertiesTemp = new Properties();
        FileInputStream fis = new FileInputStream(fileProfile);
        propertiesTemp.load(fis);
        fis.close();
        String typeProfile = propertiesTemp.getProperty("type");
        Profile profile = null;
        if(TYPE_PKCS11.equals(typeProfile) || "PKCS#11".equals(typeProfile))
            profile = new Pkcs11Profile(fileProfile);
        else if (TYPE_PKCS12.equals(typeProfile))
            profile = new Pkcs12Profile(fileProfile);
        else
            throw new RuntimeException("Tipo de perfil ["+typeProfile+"] desconocido. Archivo ["+fileProfile.getAbsolutePath()+"]");
        return profile;
    }
    
    public void setType(String type){
        properties.setProperty("type", type);
    }
    
    public void setName(String name){
        properties.setProperty("name", name);
    }
    
    public void save() throws FileNotFoundException, IOException{
        properties.store(new FileOutputStream(file),"Ejemplo");
    }
    
    public String getType(){
        return properties.getProperty("type");
    }
    
    public String getName(){
        return properties.getProperty("name");
    }
    
    public boolean isDefaultProfile(){
        return file.getName().contains(".default.");
    }
    
    public abstract BaseKeyStore getKeyStore();
    public final File getFileName() {
        return file;
    }
}