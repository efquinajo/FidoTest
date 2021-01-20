
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.PkcsN12Manager;
import gob.adsib.fido.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author ADSIB-UID
 */
public class Test9_FirmarConP12 {
    public static void main(String cor[]) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        File fileP12 = new File("C:\\Users\\GIGABYTE\\Downloads\\CERTIFICADO\\CERTIFICADO\\keysotre_gonzalo_ramiro_nuevo.p12");
        String pin = "GonzaloPwC2020*";
        String alias="gonzalo_ramiro";
        String rutaPdf = "C:\\Users\\GIGABYTE\\Downloads\\requerimientos_rata_alonzo.pdf";
        String rutaPdfFirmado = "C:\\Users\\GIGABYTE\\Downloads\\CERTIFICADO\\CERTIFICADO\\alonzo_firmado_firmado.pdf";
        
        PkcsN12Manager pkcsN12Manager = new PkcsN12Manager(fileP12);
        pkcsN12Manager.login(pin, 0);
        
        System.out.println("Lista de alias...");
        for(String aliasName: pkcsN12Manager.getAlieces()){
            System.out.println("ALIAS: "+aliasName);
        }
        System.out.println("Fin de lista");
        KeyAndCertificate keyAndCertificate = pkcsN12Manager.getKeyAndCertificate(alias);
        byte[] pdfSigned = keyAndCertificate.signPdf(IOUtil.inputStreamToBytes(new FileInputStream(rutaPdf)));
        
        FileOutputStream fos = new FileOutputStream(rutaPdfFirmado);
        fos.write(pdfSigned);
        fos.close();
        
        pkcsN12Manager.logout();
    }
}