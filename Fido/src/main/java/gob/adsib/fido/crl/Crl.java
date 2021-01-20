/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.adsib.fido.crl;

import gob.adsib.fido.Config;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 *
 * @author alberto
 */
public class Crl {
    public static X509CRLEntry obtenerEstado(X509Certificate cert) {
        try {
            // Cargar la maquinaria X.509
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // Obtener la URL para obtener la CRL a partir del certificado
            URL[] urls = getCrlURLs(cert);
            if (urls.length == 0) {
                throw new CRLException("No se dispone de ruta para la CRL");
            }
            // Obtener la lista de revocación de certificados CRL
            String crlPem = conectarSslYLeerCRL(urls[0].toString());
            // Convertir la CRL en formato pem al objeto X509CRL
            X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlPem.getBytes()));
            // Verificar que se haya cargado la CRL
            if (crl == null) {
                throw new CRLException("No se pudo obtener la CRL");
            }
            // Obtener la entrada de revocación a partir del número de serie del certificado.
            X509CRLEntry entry = (X509CRLEntry) crl.getRevokedCertificate(cert.getSerialNumber());
            return entry;
        } catch (CertificateException ex) {
            Logger.getLogger(Crl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (CRLException ex) {
            Logger.getLogger(Crl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static URL[] getCrlURLs(X509Certificate cert)
    {
        List urls = new LinkedList();
        // Obtiene la extensión ASN1 2.5.29.31
        byte[] cdp = cert.getExtensionValue("2.5.29.31");
        if (cdp != null) {
            try {
                // Mapela los datos planos en una clase
                CRLDistPoint crldp = CRLDistPoint.getInstance(X509ExtensionUtil.fromExtensionValue(cdp));
                DistributionPoint[] distPoints = crldp.getDistributionPoints();

                for (DistributionPoint dp : distPoints) {
                    GeneralNames gns = (GeneralNames) dp.getDistributionPoint().getName();
                    DERIA5String uri;
                    for (GeneralName name : gns.getNames()) {
                        // Identifica si es una URL
                        if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
                            uri = (DERIA5String) name.getName();
                            urls.add(new URL(uri.getString()));
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Crl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return (URL[])urls.toArray(new URL[urls.size()]);
    }

    public static String conectarSslYLeerCRL(String url) {
        HttpsTrustManager.allowAllSSL();
        
        URL resourceUrl;
        HttpURLConnection conn = null;
        try {
            resourceUrl = new URL(url);
            Config config = Config.getConfig();
            if (config.getProxyIp() == null) {
                conn = (HttpURLConnection) resourceUrl.openConnection();
            } else {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyIp(), config.getProxyPort() == null ? 80 : config.getProxyPort()));
                conn = (HttpURLConnection) resourceUrl.openConnection(proxy);
            }
            conn.connect();
            
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            StringBuilder builder = new StringBuilder();
            while (line!=null) {
                builder.append(line);
                builder.append("\n");
                line = reader.readLine();
            }
            return builder.toString();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("El formato de la url esta incorrecto [" + url + "]", ex);
        }
        catch (IOException ex) {
            try {
                return new String(Files.readAllBytes(Paths.get("/tmp/firmadigital_bo.crl")), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new ConnectionException("Error en la conexion con [" + url + "]", e);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {}
            }
        }
    }
}
