package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/eliminar_claves 2.05 Eliminar Par de Claves
 * @apiName TokenCargarPEM
 * @apiGroup 2 Token
 * @apiParam {String} pin El pin del token
 * @apiParam {String} alias El alias para el par de claves asociado al certificado
 * @apiParam {long} slot El número slot al que se encuentra conectado el dispositivo
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Para esta petición retorna un json vacio {}
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParam {String} pin Pin del token.
 * @apiParam {String} id El identificador de la clave privada para la cual se asociara el certificado.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *     "pin": "12345678",
 *     "alias": "3334393238383537"
 *     "slot": 0
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK
 * {
 *     "datos": {},
 *     "finalizado": true,
 *     "mensaje": "Par de claves eliminado correctamente"
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
public class RestEliminarParDeClaves extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot", Config.DEFAULT_SLOT);
            
            System.out.println("Eliminando par de claves");
            System.out.println(jsonRequest.toJSONString());
            String pin = jsonRequest.getAsString("pin");
            String alias = jsonRequest.getAsString("alias");
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));

            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            baseKeyStore.removeKeyPair(alias);
            baseKeyStore.logout();
            
            JSONObject jsonResponse = new JSONObject();            
            writeResponse(true, "Par de claves eliminado correctamente",jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}