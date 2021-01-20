package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Profile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {get} /api/profile/list Obtener lista de perfiles creados
 * @apiName TokenInfo
 * @apiGroup Profile
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Información del token
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *      "datos": [{
 *           "type": "PKCS11",
 *           "name": "Safe Net",
 *           "selected": false
 *      },{
 *           "type": "PKCS11",
 *           "name": "Safe Net",
 *           "selected": true
 *      }],
 *      "finalizado": true,
 *      "mensaje": "Lista de perfiles obtenida correctamente"
 * }
 * 
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 * 
 * @apiErrorExample Error-Response:
 * HTTP/1.1 400 Bad Request
 *  {
 *      "datos": null,
 *      "finalizado": false,
 *      "mensaje": "Descripción del error"
 *  }
 */
public class RestObtenerPerfiles extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONArray jsonArray = new JSONArray();
            List<Profile> listProfiles = Profile.getListProfile();
            for (Profile profile : listProfiles) {
                JSONObject jsonProfile = new JSONObject();
                jsonProfile.put("type", profile.getType());
                jsonProfile.put("name", profile.getName());
                jsonProfile.put("selected",profile.isDefaultProfile());
                jsonArray.add(jsonProfile);
            }            
            writeResponse(true, "Lista de perfiles obtenida correctamente", jsonArray, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}