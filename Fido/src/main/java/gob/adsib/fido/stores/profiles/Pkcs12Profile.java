package gob.adsib.fido.stores.profiles;

import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import gob.adsib.fido.stores.PkcsN12Manager;
import java.io.File;
import java.io.IOException;
import net.minidev.json.JSONObject;

/**
 *
 * @author root
 */
public class Pkcs12Profile extends Profile{
    public Pkcs12Profile(File propertyFileName) throws IOException {
        super(propertyFileName);
    }
    
    private Pkcs12Profile(File fileOutput,String name) {
        super(fileOutput,Profile.TYPE_PKCS12, name);
    }

    @Override
    public void writeDate(JSONObject jsonObject) {
        jsonObject.appendField("fileP12Path", getPathFile().getAbsolutePath());
        jsonObject.appendField("type","PKCS12");
        jsonObject.appendField("name",getName());
    }
    
    public static Pkcs12Profile newInstance(File fileOutput,String name, File pathFileKeyStore){
        Pkcs12Profile pkcs12Profile = new Pkcs12Profile(fileOutput,name);
        pkcs12Profile.properties.setProperty("fileP12Path", pathFileKeyStore.getAbsolutePath());
        pkcs12Profile.properties.setProperty("name",pkcs12Profile.getName());
        return pkcs12Profile;
    }

    public File getPathFile() {
        return new File(properties.getProperty("fileP12Path"));
    }

    @Override
    public BaseKeyStore getKeyStore() {
        return new PkcsN12Manager(getPathFile());
    }
}
