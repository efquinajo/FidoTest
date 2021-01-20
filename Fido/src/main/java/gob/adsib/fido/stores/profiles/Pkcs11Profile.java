package gob.adsib.fido.stores.profiles;

import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import gob.adsib.fido.stores.PkcsN11Manager;
import java.io.File;
import java.io.IOException;
import net.minidev.json.JSONObject;

/**
 *
 * @author root
 */
public class Pkcs11Profile extends Profile{
    public Pkcs11Profile(File propertyFileName) throws IOException {
        super(propertyFileName);
    }

    private Pkcs11Profile(File fileOutput,String name) {
        super(fileOutput,Profile.TYPE_PKCS11, name);
    }

    @Override
    public void writeDate(JSONObject jsonObject) {
        jsonObject.appendField("driverPath",getPathDriver().getAbsolutePath());
        jsonObject.appendField("type","PKCS11");
        jsonObject.appendField("name",getName());
    }
    
    public static Pkcs11Profile newInstance(File fileOutput,String name, File pathDriver, int slot){
        Pkcs11Profile pkcs11Profile = new Pkcs11Profile(fileOutput,name);
        pkcs11Profile.setPathDriver(pathDriver);
        return pkcs11Profile;
    }
    
    public void setPathDriver(File pathDriver) {
        this.properties.setProperty("driverPath", pathDriver.getAbsolutePath());
        this.properties.setProperty("type","PKCS11");
        this.properties.setProperty("name",getName());
    }

    public File getPathDriver() {
        return new File(properties.getProperty("driverPath"));
    }

    @Override
    public BaseKeyStore getKeyStore() {
        return new PkcsN11Manager(getPathDriver());
    }
}
