/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.adsib.fido.util;

import gob.adsib.fido.Config;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.pkcs_9_at_signingTime;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

/**
 *
 * @author alberto
 */
public class PKCS7Signatures {
    private JSONObject jsonSignatures = new JSONObject();

    public PKCS7Signatures(byte[] file, byte[] firma) {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray jsonArray = new JSONArray();
        jsonSignatures.put("fecha_sistema", dateFormat.format(new Date()));
        jsonSignatures.put("firmas", jsonArray);

        try {
            JSONObject jsonSignature = new JSONObject();
            JSONObject jsonCertificado = new JSONObject();
            CertificateData certificateDataIssuer = new CertificateData(new File(Config.getConfig().getPathCertificadoEmisor()));

            CMSSignedData signature = new CMSSignedData(firma);
            CMSProcessable sc = signature.getSignedContent();
            if (sc == null && file == null) {
                throw new RuntimeException("No se envio el contenido del documento firmado.");
            }
            /*if (sc != null && file != null) {
                throw new RuntimeException("Esta intentando validar una firma con contenido adjunto.");
            }*/

            CMSSignedData signedData;
            if (sc == null) {
                signedData = new CMSSignedData(new CMSProcessableByteArray(file), firma);
            } else {
                signedData = new CMSSignedData(firma);
            }
            SignerInformationStore signers = signedData.getSignerInfos();
            SignerInformation signerInfo = (SignerInformation)signers.getSigners().iterator().next();
            // Fecha firma
            Attribute attribute = signerInfo.getSignedAttributes().get(pkcs_9_at_signingTime);
            jsonSignature.put("fecha_firma", dateFormat.format(((ASN1UTCTime)attribute.getAttrValues().getObjectAt(0)).getDate()));
            // Integridad del documento
            X509Certificate cert = null;
            CertificateData certificateData = null;
            boolean integrity = false;
            Store store = signedData.getCertificates();
            Collection<X509CertificateHolder> allCerts = store.getMatches(null);
            for (X509CertificateHolder holder : allCerts) {
                cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
                certificateData = new CertificateData(cert);
                if (signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert))) {
                    integrity = true;
                    break;
                }
            }
            jsonSignature.put("documento_modificado", !integrity);
            // Cadena de confianza
            if (cert == null) {
                jsonSignature.put("cadena_de_confianza", false);
            } else {
                X509Certificate certCA = null;
                for (int i = 0; i < certificateDataIssuer.getCertificates().length; i++) {
                    try {
                        cert.verify(certificateDataIssuer.getCertificates()[i].getPublicKey());
                        certCA = certificateDataIssuer.getCertificates()[i];
                        jsonSignature.put("cadena_de_confianza", true);
                        break;
                    } catch (SignatureException ex) {
                        jsonSignature.put("cadena_de_confianza", false);
                    }
                }
                // Vigencia
                try {
                    cert.checkValidity(dateFormat.parse(jsonSignature.getAsString("fecha_firma")));
                    jsonSignature.put("firma_en_vigencia", true);
                    jsonCertificado.put("estado_fecha_firma", "valido");
                } catch (CertificateExpiredException ex) {
                    jsonSignature.put("firma_en_vigencia", false);
                    jsonCertificado.put("estado_fecha_firma", "expirado");
                } catch (CertificateNotYetValidException ex) {
                    jsonSignature.put("firma_en_vigencia", false);
                    jsonCertificado.put("estado_fecha_firma", "no_valido");
                }
                // Estado revocacion
                jsonSignature.put("certificado", jsonCertificado);
                try {
                    
                    CertificateData.CertOcspStatus certificateStatus = certificateData.verifRevocationWithOCSP((X509Certificate)certCA);

                    JSONObject jsonRevocacion = new JSONObject();
                    switch(certificateStatus.status){
                        case "REVOKED": 
                            jsonCertificado.put("resumenRevocacion", "REVOCADO");
                            jsonCertificado.put("detalleRevocacion", String.format("El certificado se encuentra revocado en fecha [%s] <br> por la razón [%s]",dateFormat.format(certificateStatus.revocationTime),certificateStatus.revocationReason));
                            jsonRevocacion.put("estado", certificateStatus.status);
                            jsonRevocacion.put("fecha", dateFormat.format(certificateStatus.revocationTime));
                            jsonRevocacion.put("razon", certificateStatus.revocationReason);
                            jsonCertificado.put("detalle_revocado", jsonRevocacion);
                            jsonCertificado.put("revocado", true);
                            jsonSignature.put("revocado", true);
                            break;
                        case "GOOD": 
                            jsonCertificado.put("resumenRevocacion", "CORRECTO");
                            jsonCertificado.put("detalleRevocacion", "El certificado no esta revocado");
                            jsonRevocacion.put("estado", certificateStatus.status);
                            jsonCertificado.put("detalle_revocado", jsonRevocacion);
                            jsonCertificado.put("revocado", false);
                            jsonSignature.put("revocado", false);
                            break;
                        case "UNKNOWN": 
                            jsonCertificado.put("resumenRevocacion", "DESCONOCIDO");
                            jsonCertificado.put("detalleRevocacion", "El certificado no se encuentra registrado en el OCSP");
                            jsonRevocacion.put("estado", certificateStatus.status);
                            jsonCertificado.put("detalle_revocado", jsonRevocacion);
                            jsonCertificado.put("revocado", false);
                            jsonSignature.put("revocado", false);
                            break;
                    }
                } catch (IOException e) {
                    jsonCertificado.put("resumenRevocacion", "NO SE PUDO VERIFICAR");
                    jsonCertificado.put("detalleRevocacion", e.getMessage());
                    jsonCertificado.put("detalle_revocado", null);
                    jsonCertificado.put("revocado", true);
                    jsonSignature.put("revocado", true);
                    e.printStackTrace();
                }
            }
            jsonSignature.put("firma_valida", ((Boolean)jsonSignature.get("cadena_de_confianza")) && !((Boolean)jsonSignature.get("revocado")) && !((Boolean)jsonSignature.get("documento_modificado")) && ((Boolean)jsonSignature.get("firma_en_vigencia")));
            jsonSignatures.put("cadena_de_confianza", jsonSignature.get("cadena_de_confianza"));
            jsonSignatures.put("modificado", !integrity);
            jsonSignatures.put("revocado", jsonSignature.get("revocado"));
            jsonSignatures.put("firma_en_vigencia", jsonSignature.get("firma_en_vigencia"));
            jsonCertificado.put("validez", new JSONObject());
            ((JSONObject)jsonCertificado.get("validez")).put("desde", dateFormat.format(cert.getNotBefore()));
            ((JSONObject)jsonCertificado.get("validez")).put("hasta", dateFormat.format(cert.getNotAfter()));
            jsonCertificado.put("titular", new JSONObject());
            ((JSONObject)jsonCertificado.get("titular")).put("DN", certificateData.getSubjectDN());
            ((JSONObject)jsonCertificado.get("titular")).put("uidNumber", certificateData.getSubjectUidNumber());
            ((JSONObject)jsonCertificado.get("titular")).put("UID", certificateData.getSubjectUID());
            ((JSONObject)jsonCertificado.get("titular")).put("CN", certificateData.getSubjectName());
            ((JSONObject)jsonCertificado.get("titular")).put("O", certificateData.getSubjectO());
            ((JSONObject)jsonCertificado.get("titular")).put("OU", certificateData.getSubjectOU());
            ((JSONObject)jsonCertificado.get("titular")).put("T", certificateData.getSubjectT());
            ((JSONObject)jsonCertificado.get("titular")).put("C", certificateData.getSubjectC());
            jsonCertificado.put("emisor", new JSONObject());
            ((JSONObject)jsonCertificado.get("emisor")).put("CN", certificateData.getIssuerName());
            ((JSONObject)jsonCertificado.get("emisor")).put("O", certificateData.getIssuerO());
            ((JSONObject)jsonCertificado.get("emisor")).put("C", certificateData.getIssuerC());

            jsonSignatures.put("documento_valido", jsonSignature.get("firma_valida"));

            jsonArray.add(jsonSignature);
        } catch (IOException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CMSException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperatorCreationException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PKCS7Signatures.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readSignature() throws IOException, CertificateException {
        /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CertificateData certificateDataIssuer = new CertificateData(new File(Config.getConfig().getPathCertificadoEmisor()));
        
        JSONArray jsonArrayFirmas = (JSONArray)jsonSignatures.get("firmas");
        
        JSONObject jsonFirma = new JSONObject();
        jsonFirma.put("revocado", false);

        Certificate pkc[] = pkcs7.getSignCertificateChain();// pk.getCertificates();
        jsonFirma.put("firma", "SIGNATURE");
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
        jsonArrayFirmas.add(jsonFirma);*/
    }

    public JSONObject getJsonSignatures() {
        return jsonSignatures;
    }
}
