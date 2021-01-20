package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.util.CertificateData;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/cargar_pem Cargar certificado PEM en el token
 * @apiName TokenCargarPEM
 * @apiGroup 2 Token
 * @apiParam {String} pem El certificado X509 en formato PEM codificado en base64
 * @apiParam {String} pin El pin del token
 * @apiParam {String} id El alias para el par de claves asociado al certificado
 * @apiParam {long} slot El número slot al que se encuentra conectado el dispositivo
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Para esta petición retorna un json vacio {}
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParam {String} pem Cadena que representa al archivo PEM en formato
 * Base64.
 * @apiParam {String} pin Pin del token.
 * @apiParam {String} id El identificador de la clave privada para la cual se asociara el certificado.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *     "pem": "LS0tLS...(BASE64)...URS0tLS0t", 
 *     "pin": "12345678",
 *     "id": "3334393238383537"
 *     "slot": 0
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK 
 * {
 *     "datos": {},
 *     "finalizado": true,
 *     "mensaje": "El certificado fue adicionado correctamente"
 * }
 *
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response 400: HTTP/1.1 400 Bad Request
 * {
 *     "datos": null, 
 *     "finalizado": false,
 *     "mensaje": "Descripción del error"
 * }
 *
 * @apiError (Error 409) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 409) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 409) {String} mensaje Descripción del conflicto interno
 * encontrado.
 *
 * @apiErrorExample Error-Response 409: HTTP/1.1 409 Conflict
 * {
 *     "datos": null,
 *     "finalizado": false,
 *     "mensaje": "No se pudo continuar debido a..."
 * }
 */
public class RestCargarCertificado extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot", Config.DEFAULT_SLOT);
            
            System.out.println("CARGAR CERTIFICADO");
            System.out.println(jsonRequest.toJSONString());
            String pin = jsonRequest.getAsString("pin");
            String pem = jsonRequest.getAsString("pem");
            String alias = jsonRequest.getAsString("id");
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));

            byte[] decoded = Base64.getDecoder().decode(pem);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate newCertificate = cf.generateCertificate(new ByteArrayInputStream(decoded));
            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            String newAlias = baseKeyStore.updateCertificate(alias, (X509Certificate) newCertificate);
            baseKeyStore.logout();
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("newAlias",newAlias);
            JSONObject jsonCertificate = new JSONObject();
            CertificateData certificateData = new CertificateData((X509Certificate)newCertificate);
            certificateData.writeInfo(jsonCertificate);
            
            jsonResponse.put("certificate",jsonCertificate);
            writeResponse(true, "El certificado fué adicionado correctamente",jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}