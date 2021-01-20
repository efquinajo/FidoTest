package gob.adsib.fido;

import gob.adsib.fido.server.HttpsServerFido;
import gob.adsib.fido.server.HttpsServerFirmatic;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import javax.swing.SwingUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
/**
 *
 * @author rcoarite, refactor Jhonny Monrroy
 */
public class AppFido {
    static {
        if(Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());
    }
    private static HttpsServerFido servidorHttpsFido;
    private static HttpsServerFirmatic servidorHttpsFirmatic;
    
    public static void main(String cor[])throws Exception{
        crearFolderPerfiles();
        System.out.println(Config.getConfig().toString());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    servidorHttpsFido = new HttpsServerFido(Config.getConfig().getPortFido());
                    System.out.println("Iniciando servicio FIDO");
                    servidorHttpsFido.start();
                    System.out.println("Servicio Fido iniciado correctamente");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if(Config.getConfig().isEnabledFirmaticServiceOnFido()){
                    System.out.println("Simulador de Firmatic HABILITADO");
                    try {
                        servidorHttpsFirmatic = new HttpsServerFirmatic();
                        servidorHttpsFirmatic.start();
                    } catch (IOException e) {
                        System.out.println("Error al iniciar simulador de FIRMATIC");
                        e.printStackTrace();
                    }                    
                }else
                    System.out.println("Simulador de Firmatic deshabilitado");
            }
        });
    }
    
    public static void stopServer(){
        if(servidorHttpsFido!=null){
            try {
                servidorHttpsFido.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(servidorHttpsFirmatic!=null){
            try {
                servidorHttpsFirmatic.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void crearFolderPerfiles(){
        File filePerfiles = Profile.getFolderProfile();
        System.out.println("Ruta Carpeta Perfiles: "+filePerfiles.getAbsolutePath());
        filePerfiles.mkdirs();
    }
}