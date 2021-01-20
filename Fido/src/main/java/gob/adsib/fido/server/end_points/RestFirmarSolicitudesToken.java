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
 * @api {post} /api/token/firmar_solicitudes Firmar Solicitudes JWS
 * @apiName TokenFirmarSolicitudes
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiParam {String} pin El pin del token.
 * @apiParam {String} alias El Alias del certificado con el cual se firmara.
 * @apiParam {Json[]} data Vector de solicitudes (payload, id, url) donde
 * <code>payload</code> es la información a firmar, <code>id</code> es el
 * identificador de la solicitud y <code>url</code> es la Url para adicionar a
 * la respuesta JWS.
 *
 * @apiSuccess {Json} datos Un vector de tuplas (jws, id) con las firmas de las
 * solicitudes en formato
 * <a href="https://es.wikipedia.org/wiki/JSON_Web_Signature" target="_blank">jws</a>
 * y sus identificacdores de solicitud.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *     "pin": "12345678", 
 *     "alias": "34928857",
 *     "slot": 0,
 *     "data": [{
 *         "payload":"---BEdddddHhhh----",
 *         "id": 4544,
 *         "url":"https://adsib.gob.bo"
 *      }]
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK
 * {
 *     "datos":[{
 *         "jws": "eyJ4...21F...fl_Aeg",
 *         "id": 4544
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
public class RestFirmarSolicitudesToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
            String pin = jsonRequest.get("pin").toString();
            String alias = jsonRequest.get("alias").toString();
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));
            JSONArray data = (JSONArray) jsonRequest.get("data");
            
            System.out.println("JSON REQUUEST: "+data.toJSONString());
            
            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            
            JSONArray jsonArrayResult = new JSONArray();
//            System.out.println("JSO DATA: "+jsonArrayResult.toJSONString());
            
            for (Object object : data) {
                JSONObject jsonElement = (JSONObject)object;
                String payloadCSR = String.valueOf(jsonElement.get("payload"));
                String payloadUrl = String.valueOf(jsonElement.getAsString("url"));
                
                String signed = keyAndCertificate.signJsonText(payloadCSR,payloadUrl);
                JSONObject jsonSigned = new JSONObject();
                jsonSigned.put("jws",signed);
                jsonSigned.put("id",jsonElement.getAsString("id"));
                jsonArrayResult.add(jsonSigned);
            }
            
            writeResponse(true, "Se firmo las solicitudes correctamente!",jsonArrayResult,HttpURLConnection.HTTP_BAD_REQUEST, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}