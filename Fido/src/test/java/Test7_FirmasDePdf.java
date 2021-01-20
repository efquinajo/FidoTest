
import gob.adsib.fido.util.PdfSignatures;
import java.io.File;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author ADSIB-UID
 */
public class Test7_FirmasDePdf {
    public static void main(String cor[]) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        
        String rutaPDF = "/root/Documentos/Depositos Token.firmado.pdf";

        PdfSignatures pdfSignatures = new PdfSignatures(new File(rutaPDF));
        System.out.println(pdfSignatures.getJsonSignatures().toJSONString());
    }
}