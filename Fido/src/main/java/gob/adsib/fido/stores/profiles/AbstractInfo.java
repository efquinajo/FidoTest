package gob.adsib.fido.stores.profiles;

import net.minidev.json.JSONObject;

/**
 *
 * @author ADSIB-UID
 */
public abstract class AbstractInfo {
    public abstract String getGlobalName();
    public abstract String getSerialID();
    public void writeData(JSONObject json){
        
    }
}