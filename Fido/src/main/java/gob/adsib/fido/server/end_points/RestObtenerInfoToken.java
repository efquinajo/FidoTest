package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Profile;
import gob.adsib.fido.stores.PkcsN11Manager;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import net.minidev.json.JSONObject;

/**
 * @api {get} /api/token/info Obtener información extra del token
 * @apiName TokenInfo
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Información del token
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *      "datos": {
 *           "token_info": {
 *               "descripcion": "FT ePass2003Auto",
 *               "serial": "2035325580030020",
 *               "label": "alvaro (User PIN)",
 *               "fabricante": "EnterSafe",
 *               "modelo": "PKCS#15"
 *           }
 *      },
 *      "finalizado": true,
 *      "mensaje": "Información de Dispositivo Criptografico obtenida correctamente"
 * }
 * 
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 * 
 * @apiErrorExample Error-Response:
 * HTTP/1.1 400 Bad Request
    {
        "datos": null,
        "finalizado": false,
	"mensaje": "Descripción del error"
    }
 *
 * @apiSampleRequest https://localhost:9000/api/token/info
 */
public class RestObtenerInfoToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros) {
        try {
            
            Profile profile = Profile.getDefaultProfile();
            if(profile == null){
                throw new IOException("No tiene ni un perfil creado en la Aplicacion FIDO. Por favor cree un perfil asociado a su token.");
            }
            if(!(profile instanceof Pkcs11Profile))
                throw new IOException("La operación es solo para perfiles de tipo PKCS11");
            Pkcs11Profile pkcs11Profile = (Pkcs11Profile) profile;
            PkcsN11Manager n11Manager = new PkcsN11Manager(pkcs11Profile.getPathDriver());
            long slots[] = n11Manager.getSlots();
            
            
            if(slots.length>1){
                throw new IOException("Se detecto mas de un dispositivo conectado. Por favor conecto solo uno.");
            }
            
            AbstractInfo info = n11Manager.getInfo(slots[0]);
            JSONObject jsonTokenInfo = new JSONObject();
            info.writeData(jsonTokenInfo);
            JSONObject jsonResponse = new JSONObject();
            
            JSONObject jsonInfoToken = new JSONObject();
            jsonInfoToken.put("descripcion",info.getGlobalName());
            jsonInfoToken.put("serial",info.getSerialID());
            jsonInfoToken.put("label",jsonTokenInfo.get("label"));
            jsonInfoToken.put("fabricante",jsonTokenInfo.get("manufacture"));
            jsonInfoToken.put("modelo",jsonTokenInfo.get("model"));
            
            jsonResponse.put("token_info", jsonInfoToken);
            
            writeResponse(true, "Información de Dispositivo Criptografico obtenida correctamente", jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}