package gob.adsib.fido.util;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import gob.adsib.fido.Config;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.tika.Tika;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 2020
 * @author ADSIB-UID
 */
public class PdfSignatures {
    private PdfReader reader = null;
    private JSONObject jsonSignatures = new JSONObject();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public PdfSignatures(File file) throws IOException, CertificateException{
        this(IOUtil.inputStreamToBytes(new FileInputStream(file)));
    }
    
    public PdfSignatures(byte[] pdfBytes) throws IOException, CertificateException{
         Tika tika = new Tika();
        String tipodeArchivo = tika.detect(new ByteArrayInputStream(pdfBytes));
        if (!tipodeArchivo.equals("application/pdf"))
            throw new IOException("El contenido no es un documento PDF");
        reader = new PdfReader(pdfBytes);
        JSONArray jsonArray = new JSONArray();
        jsonSignatures.put("fecha_sistema",dateFormat.format(new Date()));
        jsonSignatures.put("firmas",jsonArray);
        
        jsonSignatures.put("cadena_de_confianza",false);
        jsonSignatures.put("modificado",false);
        jsonSignatures.put("revocado",false);
        jsonSignatures.put("firma_en_vigencia",false);
        
        readSignatures();

        jsonSignatures.put("documento_valido",
                !((Boolean)jsonSignatures.get("revocado")) &&
                !((Boolean)jsonSignatures.get("modificado")) &&
                ((Boolean)jsonSignatures.get("cadena_de_confianza")) &&
                ((Boolean)jsonSignatures.get("firma_en_vigencia"))
        );
    }
    
    private void readSignatures() throws IOException, CertificateException{        
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        CertificateData certificateDataIssuer = new CertificateData(new File(Config.getConfig().getPathCertificadoEmisor()));
        
        JSONArray jsonArrayFirmas = (JSONArray)jsonSignatures.get("firmas");
        
        if (names.size() > 0) {
            for (int k = 0; k < names.size(); ++k) {
                JSONObject jsonFirma = new JSONObject();
                jsonFirma.put("revocado", false);
                
                String name = (String) names.get(k);
                
                if (Security.getProvider("BC") == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                
                PdfPKCS7 pkcs7 = af.verifySignature(name,"BC");
//                Calendar cal = pkcs7.getSignDate();
                Certificate pkc[] = pkcs7.getSignCertificateChain();// pk.getCertificates();
                jsonFirma.put("firma", name);
                jsonFirma.put("para_todo_el_documento", af.signatureCoversWholeDocument(name));
                jsonFirma.put("fecha_firma",dateFormat.format(pkcs7.getSignDate().getTime()));

                X509Certificate certificado = pkcs7.getSigningCertificate();
                jsonFirma.put("firma_en_vigencia",
                        certificado.getNotBefore().getTime()<=pkcs7.getSignDate().getTime().getTime() && 
                        pkcs7.getSignDate().getTime().getTime()<=certificado.getNotAfter().getTime());
                CertificateData certificateData = new CertificateData(certificado);
                
                // Verificamos cadena de confianza
                X509Certificate OCSPCert = certificateDataIssuer.getFirstCertificate();
                X509Certificate[] certs = certificateDataIssuer.getCertificates();
                for (X509Certificate cert : certs) {
                    try {
                        certificado.verify(cert.getPublicKey());
                        jsonFirma.put("cadena_de_confianza",true);
                        OCSPCert = cert;
                        break;
                    } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                        jsonFirma.put("cadena_de_confianza",false);
                        e.printStackTrace();
                    }
                }
                
                JSONObject jsonCertificado = new JSONObject();
                jsonCertificado.put("pem", certificateData.getFirstCertificateToPem());
                jsonCertificado.put("detalleRevocacion","No se realizar la verificación de revocación");
                jsonCertificado.put("nro_serie",certificado.getSerialNumber().toString(16));
                
                JSONObject jsonEmisor = new JSONObject();
                jsonEmisor.put("C",certificateData.getIssuerC());
                jsonEmisor.put("CN",certificateData.getIssuerName());
                jsonEmisor.put("O",certificateData.getIssuerO());
                jsonCertificado.put("emisor", jsonEmisor);
                
                JSONObject jsonTitular = new JSONObject();
                jsonTitular.put("UID",certificateData.getSubjectUID());
                jsonTitular.put("C",certificateData.getSubjectC());
                jsonTitular.put("T",certificateData.getSubjectT());
                jsonTitular.put("OU",certificateData.getSubjectOU());
                jsonTitular.put("uidNumber",certificateData.getSubjectUidNumber());
                jsonTitular.put("DN",certificateData.getSubjectDN());
                jsonTitular.put("CN",certificateData.getSubjectName());
                jsonTitular.put("O",certificateData.getSubjectO());
                jsonTitular.put("Description",certificateData.getSubjectDescription());
                jsonCertificado.put("titular", jsonTitular);
                
                JSONObject jsonValidez = new JSONObject();
                jsonValidez.put("desde", dateFormat.format(certificado.getNotBefore()));
                jsonValidez.put("hasta", dateFormat.format(certificado.getNotAfter()));
                jsonCertificado.put("validez", jsonValidez);
                
                try {
                    certificado.checkValidity(new Date());
                    jsonCertificado.put("certificado_vigente",true);
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                    jsonCertificado.put("certificado_vigente",false);
                }
                
                // Estado actual
                try {
                    certificado.checkValidity();
                    jsonCertificado.put("estado_actual", "valido");
                } catch (CertificateExpiredException e) {
                    jsonCertificado.put("estado_actual", "expirado");
                } catch (CertificateNotYetValidException e) {
                    jsonCertificado.put("estado_actual", "no_valido");
                }
                
                // Estado a la fecha de la firma
                try {
                    certificado.checkValidity(pkcs7.getSignDate().getTime());
                    jsonCertificado.put("estado_fecha_firma", "valido");
                } catch (CertificateExpiredException e) {
                    jsonCertificado.put("estado_fecha_firma", "expirado");
                } catch (CertificateNotYetValidException e) {
                    jsonCertificado.put("estado_fecha_firma", "no_valido");
                }
                jsonFirma.put("certificado", jsonCertificado);
                
                // Verificamos si el documento fué modificado
                try {
                    jsonFirma.put("documento_modificado", !pkcs7.verify());
                } catch (GeneralSecurityException e) {
                    jsonFirma.put("documento_modificado",true);
                    e.printStackTrace();
                }

                jsonFirma.put("verificacion_revocado", "OCSP");
                // Verificacion de revocacion
                try {
                    
                    CertificateData.CertOcspStatus certificateStatus = certificateData.verifRevocationWithOCSP(OCSPCert);

                    JSONObject jsonRevocacion = new JSONObject();
                    switch(certificateStatus.status){
                        case "REVOKED": 
                            jsonCertificado.put("resumenRevocacion","REVOCADO");
                            jsonCertificado.put("detalleRevocacion",String.format("El certificado se encuentra revocado en fecha [%s] <br> por la razón [%s]",dateFormat.format(certificateStatus.revocationTime),certificateStatus.revocationReason));
                            jsonRevocacion.put("estado",certificateStatus.status);
                            jsonRevocacion.put("fecha", dateFormat.format(certificateStatus.revocationTime));
                            jsonRevocacion.put("razon",certificateStatus.revocationReason);
                            jsonCertificado.put("detalle_revocado",jsonRevocacion);
                            jsonCertificado.put("revocado", true);
                            jsonFirma.put("revocado",true);
                            break;
                        case "GOOD": 
                            jsonCertificado.put("resumenRevocacion","CORRECTO");
                            jsonCertificado.put("detalleRevocacion","El certificado no esta revocado");
                            jsonRevocacion.put("estado",certificateStatus.status);
                            jsonCertificado.put("detalle_revocado",jsonRevocacion);
                            jsonCertificado.put("revocado", false);
                            break;
                        case "UNKNOWN": 
                            jsonCertificado.put("resumenRevocacion","DESCONOCIDO");
                            jsonCertificado.put("detalleRevocacion","El certificado no se encuentra registrado en el OCSP");
                            jsonRevocacion.put("estado",certificateStatus.status);
                            jsonCertificado.put("detalle_revocado",jsonRevocacion);
                            jsonCertificado.put("revocado", false);
                            break;
                    }
                } catch (IOException e) {
                    jsonCertificado.put("resumenRevocacion","NO SE PUDO VERIFICAR");
                    jsonCertificado.put("detalleRevocacion",e.getMessage());
                    jsonCertificado.put("detalle_revocado",null);
                    jsonCertificado.put("revocado",true);
                    jsonFirma.put("revocado",true);
                    e.printStackTrace();
                }
                // Estableciendo la validez de la firma
                
                // Procesamos el resultado final
                jsonSignatures.put("cadena_de_confianza",((Boolean)jsonSignatures.get("cadena_de_confianza"))||((Boolean)jsonFirma.get("cadena_de_confianza")));
                jsonSignatures.put("revocado",((Boolean)jsonSignatures.get("revocado"))||((Boolean)jsonFirma.get("revocado")));
                jsonSignatures.put("modificado",((Boolean)jsonSignatures.get("modificado"))||((Boolean)jsonFirma.get("documento_modificado")));
                jsonSignatures.put("firma_en_vigencia",((Boolean)jsonSignatures.get("firma_en_vigencia"))||((Boolean)jsonFirma.get("firma_en_vigencia")));
                jsonFirma.put("firma_valida",((Boolean)jsonFirma.get("cadena_de_confianza")) && !((Boolean)jsonFirma.get("revocado")) && !((Boolean)jsonFirma.get("documento_modificado")) && ((Boolean)jsonFirma.get("firma_en_vigencia")));
                // Agregamos el resultado de los datos de firma
                jsonArrayFirmas.add(jsonFirma);
            }
            
        }
    }

    public JSONObject getJsonSignatures() {
        return jsonSignatures;
    }
}