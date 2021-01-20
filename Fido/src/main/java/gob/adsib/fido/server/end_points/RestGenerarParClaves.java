package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/generate_keypar Generar nuevo par de claves
 * @apiName TokenGenerarParClaves
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 * @apiParam {String} pin El pin del token
 * @apiParam {long} slot El número slot al que se encuentra conectado el dispositivo
 * @apiSuccess {Json} datos Información de las claves privadas
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParam {String} pin Pin del token.
 *
 * @apiParamExample {json} Request-Example:
 * {
 *     "pin": "2121F21",
 *     "slot": 0
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK
 * {
 *     "datos": {
 *         "data_token": {
 *             "certificates": 0, 
 *             "data": [{
 *                 "tipo": "PRIMARY_KEY",
 *                 "tipo_desc": "CLave Privada",
 *                 "alias": "1328733034",
 *                 "tiene_certificado": false
 *             }],
 *             "private_keys": 1
 *         }
 *     },
 *     "finalizado": true,
 *     "mensaje": "Servicio iniciado correctamente"
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
public class RestGenerarParClaves extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            String pin = null;
            long slot = 0;
            if("POST".equals(httpExchange.getRequestMethod())){
                JSONObject jsonRequest = getRequestJSON(httpExchange);
                pin = jsonRequest.getAsString("pin");
                if(!jsonRequest.containsKey("slot"))
                    jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
                slot = Long.parseLong(jsonRequest.getAsString("slot"));
            }else if("GET".equals(httpExchange.getRequestMethod())){
                pin = parametros.get("pin");
                if(!parametros.containsKey("slot"))
                    parametros.put("slot","1");
                slot = Long.parseLong(parametros.get("slot"));
            }
            else
                throw new IOException("Metodo ["+httpExchange.getRequestMethod()+"] no soportado.");
            
            // Obteniendo datos del token
            Profile profile = Profile.getDefaultProfile();
            Config.validarDriverOpenSC(profile);
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            
            List<String> listAlias = baseKeyStore.getAlieces();
            int countCertificates = 0;
            int countPrivateKeys = 0;
            
            JSONObject jsonDataToken = new JSONObject();
            JSONArray jsonData = new JSONArray();
            for (String alias: listAlias) {
                KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
                countPrivateKeys++;
                // Clave Privada
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("tipo","PRIMARY_KEY");
                jsonElement.put("tipo_desc","Clave Privada");
                jsonElement.put("alias",keyAndCertificate.getAlias());
                jsonElement.put("tiene_certificado",!keyAndCertificate.isSeftSigned());
                jsonData.add(jsonElement);
                
                if(!keyAndCertificate.isSeftSigned()){
                    countCertificates++;
                    jsonElement.clear();
                    
                    jsonElement.put("tipo","X509_CERTIFICATE");
                    jsonElement.put("tipo_desc","Certificado");
                    jsonElement.put("alias",keyAndCertificate.getAlias());
                    jsonElement.put("pem",keyAndCertificate.toPemCertificate());
                    keyAndCertificate.getCertificateData().writeInfo(jsonElement);
                    jsonData.add(jsonElement);                    
                }
            }           
            
            // Generando el par de claves
            String randomAlias = BaseKeyStore.generateRandomAlias();
            KeyAndCertificate keyAndCertificate = baseKeyStore.generateKeyAndCertificate(randomAlias);
            baseKeyStore.logout();
            
            
            JSONObject jsonClaveCertificado = new JSONObject();
            jsonClaveCertificado.put("tipo","PRIMARY_KEY");
            jsonClaveCertificado.put("tipo_desc","Clave Privada");
            jsonClaveCertificado.put("alias",keyAndCertificate.getAlias());
            jsonClaveCertificado.put("tiene_certificado",false);
            
            jsonData.add(jsonClaveCertificado);
            
            
            jsonDataToken.put("certificates",countCertificates);
            jsonDataToken.put("private_keys",++countPrivateKeys);
            
            jsonDataToken.put("data", jsonData);
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("data_token",jsonDataToken);
            
            writeResponse(true, "Se genero el par de claves correctamente.", jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}
