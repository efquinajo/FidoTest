package gob.adsib.fido.server.end_points;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.common.CompleteSign;
import gob.adsib.fido.common.Signs;
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.util.CertificateData;
import gob.adsib.fido.util.StringUtils;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.bouncycastle.util.encoders.Base64;

/**
 * @api {post} /api/token/firmar_json Firmar JWS
 * @apiName TokenFirmarJson
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiParam {String} pin El pin del token.
 * @apiParam {String} alias El Alias del certificado con el cual se firmara.
 * @apiParam {Json[]} data estructura JSON con la informacion a firmar en formato base64
 *
 * @apiSuccess {Json} datos Json que contiene <code>json_firmado</code> en formato base64
 * el nombre del suscriptor <code>cn</code> y la fecha de firma<code>fecha_firma</code>
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *     "pin": "12345678", 
 *     "alias": "34928857",
 *     "slot": 0,
 *     "data": "eyJ4....(BASE64)...FzIn0="
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK
 * {
 *     "datos":["fecha_firma": "08-08-2020 20:49:41",
 *          "cn": "JUAN PEREZ",
 *          "json_firmado": "eyJhbG...(BASE64)...pTgPCw"
 *     ],
 *     "finalizado": true,
 *     "mensaje": "Se firmo lassolicitudes correctamente!"
 * }
 *
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 400 Bad Request
 * {
 *    "datos": null,
 *    "finalizado": false,
 *    "mensaje": "Descripción del error"
 * }
 */
public class RestFirmarJsonToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {            
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
            String pin = jsonRequest.get("pin").toString();
            String alias = jsonRequest.get("alias").toString();
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));
            String data = jsonRequest.getAsString("data");
            
            // transforma en arreglo de byte el archivo en base64 recibido
            byte[] dataByte = Base64.decode(data);
            
            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            
            // Crea un firmador RSA256
            JWSSigner signer = new RSASSASigner(keyAndCertificate.getPrivateKey());
            CompleteSign enviadoJson;
            boolean inicial = true;
            
            try {            
                String enviado = new String(dataByte);
                enviadoJson = (CompleteSign) StringUtils.toObject(enviado, CompleteSign.class);
                inicial = false;
            } catch (Exception ex) {
                enviadoJson = new CompleteSign();
                inicial = true;
            }
            
            
            JWSObject jwsObject;
            if (inicial) {
                //Crea un objeto JWS para firmar
                jwsObject = new JWSObject(
                                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                                new Payload(dataByte));
                enviadoJson.setPayload(new String(Base64.encode(dataByte), StandardCharsets.UTF_8));
                enviadoJson.setSignatures(new ArrayList<Signs>());
            } else {
                //Crea un objeto JWS para firmar con el payload existente
                jwsObject = new JWSObject(
                                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                                new Payload(new String(enviadoJson.getPayload())));
            }
            
            try {
                jwsObject.sign(signer);
            } catch (JOSEException ex) {
                throw new RuntimeException("Error al firmar: " + ex.getMessage());
            }            
            // Conversion del certificado de X509 a PEM para su inclusion en el flat json
            org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
            
            byte[] derCert = keyAndCertificate.getX509Certificate().getEncoded();
            String pemCert = new String(encoder.encode(derCert));
            
            baseKeyStore.logout();
            
            // Crea un objeto de firma flat, una serializacion de JWT
            Signs sign = new Signs();
            Map<String, Object> mapa = new HashMap<String, Object>();
            mapa.put("gen", "MEFP-DGSGIF");
            mapa.put("x5c", pemCert.replaceAll("(\r\n|\n)", "").toCharArray());
            sign.setHeader(mapa);
            String serial = jwsObject.serialize();
            String[] partes = serial.split("\\.");
            sign.setProtect(partes[0]);
        
            sign.setSignature(jwsObject.getSignature().toString());
            enviadoJson.getSignatures().add(sign);
                        
            String resultado = StringUtils.toJson(enviadoJson);
            resultado = Base64.toBase64String(resultado.getBytes(Charset.forName("UTF-8")));

            CertificateData certData = keyAndCertificate.getCertificateData();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String fechaFirma = dateFormat.format(new java.sql.Timestamp(calendar.getTime().getTime()));
            
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("json_firmado", resultado);
            jsonResult.put("cn", certData.getSubjectName());
            jsonResult.put("fecha_firma", fechaFirma);
            writeResponse(true, "Se firmo la solicitud correctamente!",jsonResult,HttpURLConnection.HTTP_BAD_REQUEST, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}