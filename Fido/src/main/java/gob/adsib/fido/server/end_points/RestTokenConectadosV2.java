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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


/**
 * @api {get} /api/token/connected Verificar tokens conectados
 * @apiName VerificarToken
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 * @apiDescription Obtiene la lista de token conectados de acuerdo al perfil seleccionado, el servicio obtendra el número 
 * slot al que se encuentra conectado el token y se debera enviar el mismo para las siguientes consultas
 *
 * @apiSuccess {Json} datos Información de los tokens detectados
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiParamExample {url} Request-Example: 
 * https://localhost:9000/api/token/status
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "datos": {
 *         "connected": false,
 *         "tokens": [{
 *             "slot",1
 *             "serial", "214322123242"
 *             "name": "Epass 2003"
 *         ]
 *     },
 *     "finalizado": true,
 *     "mensaje": "Lista de Tokens obtenida"
 * }
 * 
 */
public class RestTokenConectadosV2 extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
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
            
            JSONArray jsonDispositivos = new JSONArray();
            for (long slot : slots) {
                JSONObject jsonObject = new JSONObject();
                JSONObject jsonToken = (JSONObject)jsonObject;
                
                System.out.println("JSON:"+jsonToken.toJSONString());
                AbstractInfo info = n11Manager.getInfo(slot);
                jsonObject.put("slot", slot);
                jsonObject.put("name",info.getGlobalName());
                jsonObject.put("serial",info.getSerialID());
                jsonDispositivos.add(jsonObject);
            }
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("connected",!jsonDispositivos.isEmpty());
            jsonResponse.put("tokens",jsonDispositivos);
            writeResponse(true,jsonDispositivos.isEmpty()?"No se detecto ni un token conectado":"Lista de Tokens obtenida",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}