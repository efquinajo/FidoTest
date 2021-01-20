package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.stores.PkcsN11Manager;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


/**
 * @api {post} /api/token/verificar_driver Verificar Driver
 * @apiName Driver Token
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 * 
 * @apiParamExample {json} Request-Example: 
 * {
 *      "path_driver": "c://ProgramFiles/token/libreria.dll"
 * }
 *
 * @apiSuccess {Json} datos Información de los tokens detectados
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *       "datos": [{
 *           "globalName": "Prueba",
 *           "slot": 1,
 *           "token" :{
 *                "manufacture": "EnterSafe",
 *                "max_pin_length": 16,
 *                "serial": "211706F180048004",
 *                "model": "PKCS#15",
 *                "label": "FIDO_ADSIB (User PIN)",
 *                "support_opensc": false,
 *                "min_pin_length": 4
 *           }
 *       }],
 *       "finalizado": true,
 *       "mensaje": "Validación realizada correctamente"
 * }
 * 
 * @apiSampleRequest https://localhost:9000/api/token/status
 */
public class RestVerificarDriverToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            System.out.println("Request Body verificar Driver");
            System.out.println(jsonRequest.toJSONString());
            String pathDriver = jsonRequest.getAsString("path_driver");
            if(pathDriver==null)
                throw new IOException("No se envió la ruta del controlador");
            PkcsN11Manager n11Manager = new PkcsN11Manager(new File(pathDriver));
            JSONArray jsonDispositivos = new JSONArray();
            long slots[] = n11Manager.getSlots();
            
            for (long slot : slots) {
                JSONObject jsonObject = new JSONObject();
                AbstractInfo info = n11Manager.getInfo(slot);
                jsonObject.put("slot", slot);
                jsonObject.put("globalName",info.getGlobalName());
                JSONObject tokenJson = new JSONObject();
                info.writeData(tokenJson);
                jsonObject.put("token",tokenJson);
                jsonDispositivos.add(jsonObject);
            }
            
            writeResponse(true, "Validación realizada correctamente", jsonDispositivos,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}