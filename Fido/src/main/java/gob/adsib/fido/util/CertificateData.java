package gob.adsib.fido.util;

import gob.adsib.fido.crl.Crl;
import gob.adsib.fido.ocsp.CertificateResult;
import gob.adsib.fido.ocsp.OcspClient;
import gob.adsib.fido.ocsp.OcspException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;

/**
 *
 * @author ADSIB-UID
 */
public class CertificateData {
    private final List<X509Certificate> certificates = new LinkedList<X509Certificate>();

    public CertificateData(String pemCert) throws Exception {
        PEMParser pr2 = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemCert.getBytes("UTF-8"))));
        
        while (true) {
            Object o = pr2.readObject();
            if (null == o)
                break; // done
            if(!(o instanceof X509CertificateHolder)){
                throw new IOException("El texto ["+pemCert+"] no es un certificado de en formato X509");
            }
            X509Certificate x509Certificate = (new JcaX509CertificateConverter().getCertificate((X509CertificateHolder)o));
            certificates.add(x509Certificate);
        }
    }
    
    public CertificateData(X509Certificate certificate) {
        certificates.add(certificate);
    }
    
    public CertificateData(File fileCertificate) throws IOException, CertificateException{
        PEMParser pr2 = new PEMParser(new InputStreamReader(new FileInputStream(fileCertificate)));
        
        while (true) {
            Object o = pr2.readObject();
            if (null == o)
                break; // done
            if(!(o instanceof X509CertificateHolder)){
                throw new IOException("El archivo ["+fileCertificate.getAbsolutePath()+"] no es un certificado de en formato X509");
            }
            X509Certificate x509Certificate = (new JcaX509CertificateConverter().getCertificate((X509CertificateHolder)o));
            certificates.add(x509Certificate);
        }
    }

    public String getIssuerName(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getIssuer();
            if (x500name.getRDNs(BCStyle.CN).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.CN)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getIssuerOU(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getIssuer();
            if (x500name.getRDNs(BCStyle.OU).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.OU)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getIssuerO() {
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getIssuer();
            if (x500name.getRDNs(BCStyle.O).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.O)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getIssuerC()  {
        try {            
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getIssuer();
            if (x500name.getRDNs(BCStyle.C).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.C)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getIssuerMail(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getIssuer();
            if (x500name.getRDNs(BCStyle.EmailAddress).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.EmailAddress)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSubjectName(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.CN).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.CN)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSubjectOU(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.OU).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.OU)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSubjectDN(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.DN_QUALIFIER).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.DN_QUALIFIER)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getSubjectT() {
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.T).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.T)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getSubjectUID(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.UID).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.UID)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getSubjectC(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.C).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.C)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getSubjectUidNumber(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(new ASN1ObjectIdentifier("1.3.6.1.1.1.1.0")).length > 0) {
                RDN cn = x500name.getRDNs(new ASN1ObjectIdentifier("1.3.6.1.1.1.1.0"))[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getSubjectDescription(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(new ASN1ObjectIdentifier("2.5.4.13")).length > 0) {
                RDN cn = x500name.getRDNs(new ASN1ObjectIdentifier("2.5.4.13"))[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getSubjectDnQualifier(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(new ASN1ObjectIdentifier("2.5.4.46")).length > 0) {
                RDN cn = x500name.getRDNs(new ASN1ObjectIdentifier("2.5.4.46"))[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSubjectO(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.O).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.O)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSubjectMail(){
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificates.get(0)).getSubject();
            if (x500name.getRDNs(BCStyle.EmailAddress).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.EmailAddress)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            }
            return null;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retorn el nro de serie del certificado
     * @return El nro de serie en String
     */
    public String getSerialHex() {
        BigInteger bi = certificates.get(0).getSerialNumber();
        return bi.toString(16);
    }

    public X509Certificate[] getCertificates() {
        X509Certificate[] res = new X509Certificate[certificates.size()];
        int i = 0;
        for (X509Certificate cert : certificates) {
            res[i++] = cert;
        }
        return res;
    }
    
    public class CertOcspStatus{
        public final String status;
        public final Date revocationTime;
        public final String revocationReason;

        public CertOcspStatus(String status, Date revocationTime, String revocationReason) {
            this.status = status;
            this.revocationTime = revocationTime;
            this.revocationReason = revocationReason;
        }

        @Override
        public String toString() {
            return "CertOcspStatus{" + "status=" + status + ", revocationTime=" + revocationTime + ", revocationReason=" + revocationReason + '}';
        }
    }

    public CertOcspStatus verifRevocationWithOCSP(X509Certificate issuerCertificate) throws IOException{
        List<X509Certificate> intermediates = new LinkedList<>();
        intermediates.add(issuerCertificate);

        OcspClient ocspClient = OcspClient.builder()
                .set(OcspClient.INTERMEDIATES, intermediates)
                .build();

        try {
            CertificateResult certificateResult = ocspClient.verify(certificates.get(0));

            if (certificateResult == null) {
                return new CertOcspStatus("UNKNOWN", null, null);
            }

            switch (certificateResult.getStatus()) {
                case REVOKED:
                    return new CertOcspStatus("REVOKED", certificateResult.getRevocationTime(), certificateResult.toString());
                case GOOD:
                    return new CertOcspStatus("GOOD", null, null);
                default:
                    return new CertOcspStatus("UNKNOWN", null, null);
            }
        } catch (OcspException ex) {
            try {
                X509CRLEntry entry = Crl.obtenerEstado(certificates.get(0));
                if (entry == null) {
                    return new CertOcspStatus("GOOD", null, null);
                }
                switch (entry.getRevocationReason()) {
                    case UNSPECIFIED:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "No especificado.");
                    case KEY_COMPROMISE:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "Se sabe o se sospecha que la clave privada del signatario se ha visto comprometida.");
                    case CA_COMPROMISE:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "Se sabe o se sospecha que la autoridad de certificación puede haber sido comprometida.");
                    case AFFILIATION_CHANGED:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "El nombre u otra información del signatario ha cambiado.");
                    case SUPERSEDED:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "El certificado ha sido sustituído.");
                    case CESSATION_OF_OPERATION:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "El certificado ya no es necesario.");
                    case CERTIFICATE_HOLD:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "El certificado ha sido puesto en espera.");
                    case UNUSED:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "El certificado se encuentra en desuso.");
                    case REMOVE_FROM_CRL:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "Se removió de la CRL.");
                    case PRIVILEGE_WITHDRAWN:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "Privilegios retirados.");
                    case AA_COMPROMISE:
                        return new CertOcspStatus("REVOKED", entry.getRevocationDate(), "Se sabe o se sospecha que la clave privada del emisor se ha visto comprometida.");
                    default:
                        return null;
                }
            } catch (Exception e) {
                throw new IOException("Error al consultar servidor OCSP");
            }
        }
    }
    
    private ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    public String getOcspUrl() {
        ASN1Primitive obj;
        try {
            obj = getExtensionValue(getFirstCertificate(), Extension.authorityInfoAccess.getId());
        } catch (IOException ex) {
            return null;
        }

        if (obj == null) {
            return null;
        }

        AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(obj);

        AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
        for (AccessDescription accessDescription : accessDescriptions) {
            boolean correctAccessMethod = accessDescription.getAccessMethod().equals(X509ObjectIdentifiers.ocspAccessMethod);
            if (!correctAccessMethod) {
                continue;
            }

            GeneralName name = accessDescription.getAccessLocation();
            if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
                continue;
            }

            DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
            return derStr.getString();
        }

        return null;
    }
    
    public void writeInfo(JSONObject jsonCertificado) throws Exception{
//        jsonCertificado.put("pem",keyAndCertificate.toPemCertificate());
        jsonCertificado.put("common_name",getSubjectName());
        jsonCertificado.put("serialNumber",getSerialHex());

        // Validez de certificado
        JSONObject jsonValidez = new JSONObject();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        jsonValidez.appendField("desde",dateFormat.format(certificates.get(0).getNotBefore()));
        jsonValidez.appendField("hasta",dateFormat.format(certificates.get(0).getNotAfter()));
        jsonCertificado.put("validez",jsonValidez);

        // Datos de titular de certificado
        JSONObject jsonTitular = new JSONObject();
        
        jsonTitular.appendField("CN",getSubjectName());
        jsonTitular.appendField("EmailAddress",getSubjectMail());
        jsonTitular.appendField("O",getSubjectO());
        jsonTitular.appendField("OU",getSubjectOU());
        jsonTitular.appendField("T",getSubjectT());
        jsonTitular.appendField("UID",getSubjectUID());
        jsonTitular.appendField("uidNumber", getSubjectUidNumber());
        jsonTitular.appendField("dnQualifier", getSubjectDnQualifier());
        jsonTitular.appendField("C", getSubjectC());
        jsonTitular.appendField("description", getSubjectDescription());
//          "titular": {
//            "UID": "1H",
//            "C": "BO",
//            "T": "Director Ejecutivo",
//            "OU": "Dirección",
//            "uidNumber": "12735834",
//            "DN": "CI",
//            "CN": "Sylvain Damien Lesage",
//            "O": "ADSIB"
//          },
//          "emisor": {
//            "C": "BO",
//            "CN": "Entidad Certificadora Publica ADSIB",
//            "O": "ADSIB"
//          }
        jsonCertificado.put("titular",jsonTitular);
        
        JSONObject jsonEmisor = new JSONObject();
        jsonEmisor.appendField("CN",getIssuerName());
        jsonEmisor.appendField("O",getIssuerO());
        
        jsonCertificado.put("emisor",jsonEmisor);
    }

    public X509Certificate getFirstCertificate() {
        return certificates.get(0);
    }
    
    public String getFirstCertificateToPem() throws CertificateEncodingException, IOException{
        StringBuilder builder= new StringBuilder();
        builder.append("-----BEGIN CERTIFICATE-----");
        builder.append("\n");
        Base64Util base64Util = new Base64Util();
        base64Util.setLineLength(64);
        builder.append(base64Util.encode(getFirstCertificate().getEncoded()));
        builder.append("\n");
        builder.append("-----END CERTIFICATE-----");
        return builder.toString();
    }
}
