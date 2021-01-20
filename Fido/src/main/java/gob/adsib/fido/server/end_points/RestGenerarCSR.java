package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import java.net.HttpURLConnection;
import java.util.HashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/generate_csr Generar CSR
 * @apiName TokenGenerarCSR
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos El CSR obtenido
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParam {Json[]} subject Vector de tuplas (OID,Valor) para generar los CSR.
 * @apiParam {String} pin Pin del token.
 * @apiParam {String} alias_certificado El alias de la clave privada para el
 * cual se aosciara esta solicitud de sertificado
 *
 * @apiParamExample {json} Request-Example: { "subject": [ { "oid":"2.5.4.46",
 * "value":"CI" },{ "oid":"2.5.4.3", "value":"Ronald Coarite Mamani" },{
 * "oid":"2.5.4.12", "value":"Consultor" },{ "oid":"1.3.6.1.1.1.1.0",
 * "value":"6817702" },{ "oid":"2.5.4.10", "value":"AGETIC" },{
 * "oid":"2.5.4.11", "value":"Bajo seguencoma calle Miranda" },{
 * "oid":"2.5.4.6", "value":"BO" },{ "oid":"0.9.2342.19200300.100.1.1",
 * "value":"1M" } ], "pin": "12345678", "alias_certificado": "1328733034",
 * "slot": 0 }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK { "datos": { "csr":
 * "-----BEGIN CERTIFICATE REQUEST-----\n MIn5HgH3Lpzsm8njCYxOwH....
 * vrtmYNVlM9MVvwIvsQ8074Y0MI\n -----END CERTIFICATE REQUEST-----\n" },
 * "finalizado": true, "mensaje": "Se genero el CSR correctamente" }
 *
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 400 Bad Request { "datos": null,
 * "finalizado": false, "mensaje": "Descripción del error" }
 *
 * @apiError (Error Critico 500) {Object} datos Retorna <code>null</code> el el
 * caso de error.
 * @apiError (Error Critico 500) {Boolean} finalizado Retorna <code>false</code>
 * el el caso de error.
 * @apiError (Error Critico 500) {String} mensaje Descripción del error critico
 * encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 500 Internal Server Error {
 * "datos": null, "finalizado": false, "mensaje": "Descripción del error
 * critico" }
 */
@SuppressWarnings("restriction")
public class RestGenerarCSR extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros) {
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            if (!jsonRequest.containsKey("slot")) {
                jsonRequest.appendField("slot", Config.DEFAULT_SLOT);
            }
            System.out.println(jsonRequest.toJSONString());
            String pin = jsonRequest.getAsString("pin");
            JSONArray subject = (JSONArray) jsonRequest.get("subject");
            String alias = jsonRequest.getAsString("alias_certificado");
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));

            HashMap<String, String> params = new HashMap<String, String>(parametros);

            for (int i = 0; i < subject.size(); i++) {
                JSONObject jsonOidValue = (JSONObject) subject.get(i);
                System.out.println(jsonOidValue.toJSONString());
                params.put(jsonOidValue.getAsString("oid"), jsonOidValue.getAsString("value"));
            }

            Profile profile = Profile.getDefaultProfile();
            Config.validarDriverOpenSC(profile);
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin, slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            String pemCSR = keyAndCertificate.generateCsr(params);
            baseKeyStore.logout();

            // Escribiendo respuesta
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("csr", pemCSR);
            writeResponse(true, "Se genero el CSR correctamente", jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);

        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        } finally {
            httpExchange.close();
        }
    }
}