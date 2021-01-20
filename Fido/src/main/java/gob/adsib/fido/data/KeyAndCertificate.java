package gob.adsib.fido.data;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import gob.adsib.fido.Config;
import gob.adsib.fido.common.DateUtil;
import gob.adsib.fido.util.Base64Util;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import gob.adsib.fido.util.CertificateData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 *
 * @author Ronald Coarite Mamani
 */
public class KeyAndCertificate {
    private final PrivateKey privateKey;
    private final X509Certificate x509Certificate;
    private final String alias;

    public KeyAndCertificate(PrivateKey privateKey, X509Certificate x509Certificate, String alias) {
        this.privateKey = privateKey;
        this.x509Certificate = x509Certificate;
        this.alias = alias;
    }
    
    public KeyAndCertificate(String pemPrivateKey, String pemX509Certificate, String alias) throws IOException, CertificateException {
        // Para el certificado
        PEMParser pr2 = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemX509Certificate.getBytes("UTF-8"))));
        Object o = pr2.readObject();
        if(!(o instanceof X509CertificateHolder)){
            throw new IOException("El texto ["+pemX509Certificate+"] no es un certificado de en formato X509");
        }
        x509Certificate = (new JcaX509CertificateConverter().getCertificate((X509CertificateHolder)o));
        
        // Para la clave privada
        PEMParser privatekeyParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemPrivateKey.getBytes("UTF-8"))));
        Object oPrivate = privatekeyParser.readObject();
        JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
        if (oPrivate instanceof PEMKeyPair) {
            PEMKeyPair pemKeyPair = (PEMKeyPair) oPrivate;
            KeyPair keyPair = jcaPEMKeyConverter.getKeyPair(pemKeyPair);
            privateKey = keyPair.getPrivate();
            // Verificamos si la clave privada corresponde al Certificado
            PublicKey publicKey = keyPair.getPublic();
            RSAPublicKey publicKeyLastCertificate = (RSAPublicKey)publicKey;
            RSAPublicKey publicKeyNewCertificate = (RSAPublicKey)x509Certificate.getPublicKey();
        
            String lastMod = publicKeyLastCertificate.getModulus().toString(16);
            String newMod = publicKeyNewCertificate.getModulus().toString(16);
            if(!lastMod.equals(newMod))
                throw new IOException("El certificado no corresponde al Par de Claves");
            
        } else if (oPrivate instanceof PrivateKeyInfo) {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) oPrivate;
            privateKey = jcaPEMKeyConverter.getPrivateKey(privateKeyInfo);
        } else {
            throw new IOException("El texto ["+pemPrivateKey+"] no es una clave privada");
        }
        this.alias = alias;
    }
    
    public String generateCsr(Map<String,String> params) throws Exception{        
        X500NameBuilder nameBuilder = new X500NameBuilder();
        for (Map.Entry<String, String> item : params.entrySet()) {
            String oidID = item.getKey();
            String oidValue = item.getValue();
            
            ASN1ObjectIdentifier objectIdentifier = new ASN1ObjectIdentifier(oidID);
            nameBuilder.addRDN(objectIdentifier, oidValue);
        }
        
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(nameBuilder.build(),x509Certificate.getPublicKey());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(privateKey);
        PKCS10CertificationRequest csr = p10Builder.build(signer);

        StringWriter w = new StringWriter();
        JcaPEMWriter p = new JcaPEMWriter(w);
        p.writeObject(csr);
        p.close();
        
        String csrResult = w.toString();
        csrResult=csrResult.replace("\r", "");
        return csrResult;
    }
    
    
    public void signXml(byte[] xmlDate,String alias) throws Exception{
        
    }
    
    public String signJson(JSONObject jsonPayload,String urlCert) throws Exception{
        JWSSigner jwsSigner = new RSASSASigner(privateKey);
        JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
        if (urlCert != null) {
            builder.x509CertURL(new URI(urlCert));
        }
                                            
        JWSObject jwsObject = new JWSObject(builder.build(),new Payload(jsonPayload));
        // Compute the RSA signature
        jwsObject.sign(jwsSigner);
        
        return jwsObject.serialize();
    }
    
    public String signJsonText(String jsonPayload,String urlCert) throws Exception{
        JWSSigner jwsSigner = new RSASSASigner(privateKey);
        JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
        if (urlCert != null) {
            builder.x509CertURL(new URI(urlCert));
        }
                                            
        JWSObject jwsObject = new JWSObject(builder.build(),new Payload(jsonPayload));
        // Compute the RSA signature
        jwsObject.sign(jwsSigner);
        
        return jwsObject.serialize();
    }
    
    public byte[] signPdf(byte[] pdfInBytes) throws Exception{
        PdfReader reader = new PdfReader(pdfInBytes,null);
        ByteArrayOutputStream baobs = new ByteArrayOutputStream();
        // verificamos si ya esta firmado
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        PdfStamper stp;
        if(names.size()>0) {
            // "El documento ya tiene una o mas firmas"
            stp = PdfStamper.createSignature(reader, baobs, '\0', null, true);
        }else {
            // "Es la primera firma"
            stp = PdfStamper.createSignature(reader, baobs, '\0');
        }
        PdfSignatureAppearance sap = stp.getSignatureAppearance();

        //sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        ExternalSignature es = new PrivateKeySignature(privateKey, "SHA-256", null);
        ExternalDigest digest = new BouncyCastleDigest();
        Certificate[] chain = {x509Certificate};
        MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
        stp.close();
        // Obtenemos el PDF firmado
        byte[] pdfSignedBytes = baobs.toByteArray();
        return pdfSignedBytes;
    }

    public byte[] signPKCS7(byte[] bytes) throws Exception {
        // https://stackoverflow.com/questions/10424968/add-signed-authenticated-attributes-to-cms-signature-using-bouncycastle
        List<Certificate> certlist = new ArrayList<Certificate>();
        certlist.add(x509Certificate);
        Store certstore = new JcaCertStore(certlist);
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").
                build()).build(signer, (X509Certificate) x509Certificate));
        generator.addCertificates(certstore);

        CMSTypedData cmsdata = new CMSProcessableByteArray(bytes);
        //CMSSignedData signeddata = generator.generate(cmsdata, false);
        CMSSignedData signeddata = generator.generate(cmsdata, true);
        return signeddata.getEncoded();
    }

    public String getAlias() {
        return alias;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    public PublicKey getPublicKey(){
        return x509Certificate.getPublicKey();
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }
    
    public boolean isSeftSigned(){
        try {
            x509Certificate.verify(x509Certificate.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isAdsibSigned(){
        try {
            CertificateData certificateDataIssuer = new CertificateData(new File(Config.getConfig().getPathCertificadoEmisor()));
            x509Certificate.verify(certificateDataIssuer.getFirstCertificate().getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String toPemCertificate() throws CertificateEncodingException{
        StringBuilder builder= new StringBuilder();
        builder.append("-----BEGIN CERTIFICATE-----");
        builder.append("\n");
        Base64Util base64Util = new Base64Util();
        base64Util.setLineLength(64);
        builder.append(base64Util.encode(x509Certificate.getEncoded()));
        builder.append("\n");
        builder.append("-----END CERTIFICATE-----");
        return builder.toString();
    }
    
    public CertificateData getCertificateData()throws Exception{
        return new CertificateData(x509Certificate);
    }

    public byte[] signPdfStamp(byte[] pdfInBytes,int coorX0,int coorY0,int coorX1
            ,int coorY1,String fieldName,String reason,int page,String location) throws Exception{
        PdfReader reader = new PdfReader(pdfInBytes,null);
        ByteArrayOutputStream baobs = new ByteArrayOutputStream();
        // verificamos si ya esta firmado
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        PdfStamper stp;
        if(names.size()>0) {
            // "El documento ya tiene una o mas firmas"
            stp = PdfStamper.createSignature(reader, baobs, '\0', null, true);
        }else {
            // "Es la primera firma"
            stp = PdfStamper.createSignature(reader, baobs, '\0');
        }
        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        sap.setReason(reason);
        sap.setLocation(location);
        sap.setVisibleSignature(new Rectangle(coorX0,coorY0,coorX1,coorY1),page,fieldName);

        ExternalSignature es = new PrivateKeySignature(privateKey, "SHA-256", null);
        ExternalDigest digest = new BouncyCastleDigest();
        Certificate[] chain = {x509Certificate};
        MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
        stp.close();
        // Obtenemos el PDF firmado
        byte[] pdfSignedBytes = baobs.toByteArray();
        return pdfSignedBytes;
    }

    public byte[] signPdfStampImg(byte[] pdfInBytes,int xOne,int yOne,int page) throws Exception{
        PdfReader reader = new PdfReader(pdfInBytes,null);
        ByteArrayOutputStream baobs = new ByteArrayOutputStream();
        // verificamos si ya esta firmado
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        PdfStamper stp;
        if(names.size()>0) {
            // "El documento ya tiene una o mas firmas"
            stp = PdfStamper.createSignature(reader, baobs, '\0', null, true);
        }else {
            // "Es la primera firma"
            stp = PdfStamper.createSignature(reader, baobs, '\0');
        }
        PdfSignatureAppearance sap = stp.getSignatureAppearance();

        //Adicionando logo de la empresa
        int widthImgSignature=60;//100
        int heightImgSignature=20;//60
        Image sigImg = Image.getInstance("D:\\logo.jpg");
        sigImg.scaleToFit(widthImgSignature, heightImgSignature);
        sigImg.setAbsolutePosition(xOne, yOne);
        PdfContentByte over = stp.getOverContent(page);
        over.addImage(sigImg);

        //Recuperando datos del certificado
        CertificateData certificateData=new CertificateData(x509Certificate);
        String fullName = certificateData.getSubjectName();
        String cargo = certificateData.getSubjectT();

        //Adicionando datos del firmante
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD,BaseFont.WINANSI,BaseFont.EMBEDDED);
        over.saveState();
        over.beginText();
        over.setColorFill(new BaseColor(128, 128, 128));
        over.setFontAndSize(bf, 6);//4
        int xMidImg = xOne + (widthImgSignature / 2);
        int ySpace = yOne - 6;
        int ySpaceBetweenLine = 6;//4
        int rotation = 360;
        String currentDate = DateUtil.toString(DateUtil.FORMAT_DATE_TIME,new Date());

        boolean swCargoSplit=false;String cargoOne="";String cargoTwo="";
        if(cargo.length()>fullName.length()){
            if(cargo.substring(fullName.length(),fullName.length()+1).equals(" ")){
                cargoOne = cargo.substring(0,fullName.length());
                cargoTwo = cargo.substring(fullName.length(),cargo.length()).trim();
            }else {
                int pivote = cargo.substring(0, fullName.length()).lastIndexOf(" ");
                cargoOne = cargo.substring(0,pivote);
                cargoTwo = cargo.substring(pivote,cargo.length()).trim();
            }
            swCargoSplit = true;
        }

        over.showTextAligned(Element.ALIGN_CENTER,DateUtil.capitalize(fullName), xMidImg, ySpace - ySpaceBetweenLine * 0, rotation);
        if(!swCargoSplit) {
            over.showTextAligned(Element.ALIGN_CENTER, DateUtil.capitalize(cargo), xMidImg, ySpace - ySpaceBetweenLine * 1, rotation);
            over.showTextAligned(Element.ALIGN_CENTER, currentDate, xMidImg, ySpace - ySpaceBetweenLine * 2, rotation);
        }else{
            over.showTextAligned(Element.ALIGN_CENTER, DateUtil.capitalize(cargoOne), xMidImg, ySpace - ySpaceBetweenLine * 1, rotation);
            over.showTextAligned(Element.ALIGN_CENTER, DateUtil.capitalize(cargoTwo), xMidImg, ySpace - ySpaceBetweenLine * 2, rotation);
            over.showTextAligned(Element.ALIGN_CENTER, currentDate, xMidImg, ySpace - ySpaceBetweenLine * 3, rotation);
        }

        over.endText();
        over.restoreState();

        ExternalSignature es = new PrivateKeySignature(privateKey, "SHA-256", null);
        ExternalDigest digest = new BouncyCastleDigest();
        Certificate[] chain = {x509Certificate};
        MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);

        stp.close();
        reader.close();
        // Obtenemos el PDF firmado
        byte[] pdfSignedBytes = baobs.toByteArray();
        return pdfSignedBytes;
    }
}