package gob.adsib.fido.util.os;

import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author ADSIB-UID
 */
public abstract class AbstractOs {
    public abstract String getDefaultBrowser();
    
    public static String getUserDirHome(){
//        System.getProperty("user.home")
        return FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
    }
    
    public static String getOsArchitecture(){
        String archi = System.getProperty("os.arch");
        if(archi.contains("64"))
            return "64";
        if(archi.contains("32"))
            return "32";
        throw new RuntimeException("Nombre de arquitectura desconocido");
    }
}