package gob.adsib.fido.util;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;


/**
 *
 * @author root
 */
public class SignerPDF {
    /**
     * Metodo que crea una firma vacia en un documento PDF, la misma se crea inicialmente para 
     * ser posteriormente ser actualizada con la firma del hash en un PKCS7.
     * El proceso que cumple hata finalizar la firma es la siguiente.
     * 
     * 1.- Obtención del certificado del token. Puede ser obtenida en el momento de la firma o almacenada previo a la firma
     * 2.- Creacion de una firma vacia del documento PDF en el servidor
     * 3.- Obtención del HASH del documento (servidor)
     * 4.- Enviado del hash para su firma con el token al cliente (servidor)
     * 5.- Firmado del hash con PKCS7 en el lado del cliente
     * 6.- Enviar la firma as servidor (cliente)
     * 7.- Actualizar el documento pdf con la firma (servidor)
     * 
     * @param entradaPDF El Flujo de entrada del documento PDF
     * @param salidaPDF El Flujo de salida donde se escribira el documento que tiene la firma vacia
     * @param aliasDeFirma EL alias para identicar y posteriormente actualizar con el mimos alias una vez que se tiene la firma
     * @param certificado El certificado con el que se creara la firma vacia
     * @param hashAlgoritmo Algoritmo que se usara para la firma. Recomendado SHA-256
     * @return un array de bytes con el hash del PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public static byte[] crearFirmaVaciaDePDF(InputStream entradaPDF, OutputStream salidaPDF, String aliasDeFirma, Certificate certificado,String hashAlgoritmo) throws IOException, DocumentException, GeneralSecurityException {
        PdfReader reader = new PdfReader(entradaPDF);
        PdfStamper stamper = PdfStamper.createSignature(reader,salidaPDF, '\0');
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, aliasDeFirma);
        appearance.setCertificate(certificado);
        ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
        MakeSignature.signExternalContainer(appearance, external, 8192);
        
        // Obteniendo el PKCS7 para ser firmado
        InputStream inputStream = appearance.getRangeStream();
        
        return obtenerHash(inputStream, hashAlgoritmo);
    }

    /**
     * Obtiene le hash de un flujo de entrada con el respectivo algoritmo
     * @param inputStream El Flujo de entrada para obtener el hash
     * @param hashAlgorithm EL algoritmo empleado para obtener el hash. Recomendado SHA-256
     * @return un array de bytes con el hash del PDF
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public static byte[] obtenerHash(InputStream inputStream,String hashAlgorithm) throws GeneralSecurityException, IOException
    {
        BouncyCastleDigest digest = new BouncyCastleDigest();
        byte[] hash = DigestAlgorithms.digest(inputStream, digest.getMessageDigest(hashAlgorithm));
        return hash;
    }

    /**
     * Metodo actualiza la firma de un cocumento con la firma vacia para ser actualizada 
     * 
     * 1.- Obtención del certificado del token. Puede ser obtenida en el momento de la firma o almacenada previo a la firma
     * 2.- Creacion de una firma vacia del documento PDF en el servidor
     * 3.- Obtención del HASH del documento (servidor)
     * 4.- Enviado del hash para su firma con el token al cliente (servidor)
     * 5.- Firmado del hash con PKCS7 en el lado del cliente
     * 6.- Enviar la firma as servidor (cliente)
     * 7.- Actualizar el documento pdf con la firma (servidor)
     * 
     * @param entradaPDF El Flujo de entrada del documento PDF con la firma vacia
     * @param salidaPDF El Flujo de salida donde se escribira el documento que tiene la firma
     * @param aliasFirma EL alias con el que se creo la firma vacia para actualizar la firma
     * @param signaturePKCS7 El hash firmado con PKCS7
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public static void actualizarFirmaVaiciaPDF(InputStream entradaPDF, OutputStream salidaPDF,String aliasFirma,byte[] signaturePKCS7) throws IOException, DocumentException, GeneralSecurityException
    {
        PdfReader reader = new PdfReader(entradaPDF);
        MakeSignature.signDeferred(reader, aliasFirma, salidaPDF, new ExternalSignature(signaturePKCS7));
    }
    
    /**
     * Clase utilitaria para sobrecargar y actualizar la firma.
     */
    public static class ExternalSignature implements ExternalSignatureContainer 
    {
        private final byte[] signature;
        
        public ExternalSignature(byte[] signature) {
            this.signature = signature;
        }
        
        @Override
        public byte[] sign(InputStream in) throws GeneralSecurityException {
            return signature;
        }

        @Override
        public void modifySigningDictionary(PdfDictionary pd) {
            
        }
    }
    
    public static byte[] firmarTexto(PrivateKey privateKey,X509Certificate x509Certificate,String hashAlgorithm,byte[] hash) throws GeneralSecurityException, IOException {
        PrivateKeySignature signature = new PrivateKeySignature(privateKey, hashAlgorithm,null);
        BouncyCastleDigest digest = new BouncyCastleDigest();
        PdfPKCS7 sgn = new PdfPKCS7(null, new Certificate[]{x509Certificate}, hashAlgorithm, "BC", digest, false);
//        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash,  null, null, MakeSignature.CryptoStandard.CMS);
        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, Calendar.getInstance(), null, null, MakeSignature.CryptoStandard.CMS);
        byte[] extSignature = signature.sign(sh);
        sgn.setExternalDigest(extSignature, null, signature.getEncryptionAlgorithm());
//        return sgn.getEncodedPKCS7(hash,null, null, null, MakeSignature.CryptoStandard.CMS);
        return sgn.getEncodedPKCS7(hash,Calendar.getInstance(), null, null, null, MakeSignature.CryptoStandard.CMS);
    }

}