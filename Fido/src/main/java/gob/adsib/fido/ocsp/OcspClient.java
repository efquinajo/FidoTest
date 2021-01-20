package gob.adsib.fido.ocsp;

import net.klakegg.pkix.ocsp.builder.BuildHandler;
import net.klakegg.pkix.ocsp.builder.Builder;
import net.klakegg.pkix.ocsp.builder.Properties;
import net.klakegg.pkix.ocsp.builder.Property;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

/**
 * Implementation of OCSP client supporting verification of a single certificate.
 *
 * @author erlend
 */
public class OcspClient extends AbstractOcspClient {

    public static final Property<Boolean> EXCEPTION_ON_REVOKED = Property.create(true);

    public static final Property<Boolean> EXCEPTION_ON_UNKNOWN = Property.create(true);

    /**
     * Builder to create an instance of the client.
     *
     * @return Prepared client.
     */
    public static Builder<OcspClient> builder() {
        return new Builder(new BuildHandler<OcspClient>() {
            @Override
            public OcspClient build(Properties properties) {
                return new OcspClient(properties);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    private OcspClient(Properties properties) {
        super(properties);
    }

    public CertificateResult verify(X509Certificate certificate) throws OcspException {
        return verify(certificate, findIntermediate(certificate));
    }

    public CertificateResult verify(X509Certificate certificate, X509Certificate issuer) throws OcspException {
        return verify(CertificateIssuer.generate(issuer), certificate);
    }

    public CertificateResult verify(CertificateIssuer issuer, X509Certificate certificate) throws OcspException {
        URI uri = properties.get(OVERRIDE_URL);

        if (uri == null) {
            uri = detectOcspUri(certificate);

            // In case no URI was detected.
            if (uri == null)
                try {
                    uri = new URI("http://www.firmadigital.bo/ocsp/");
                } catch (URISyntaxException ex) {
                    throw new OcspException(ex.getMessage(), ex);
                }
        }

        return verify(uri, issuer, certificate.getSerialNumber());
    }

    public CertificateResult verify(URI uri, CertificateIssuer issuer, BigInteger serialNumber) throws OcspException {
        OcspRequest request = new OcspRequest();
        request.setIssuer(issuer);
        request.addCertificates(serialNumber);
        if (properties.get(NONCE))
            request.addNonce();

        OcspResponse response = fetch(request, uri);
        response.verifyResponse();

        CertificateResult certificateResult = response.getResult().get(serialNumber);

        return certificateResult;
    }
}
