package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.stores.opensc.Pkcs11Scan;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import javax.smartcardio.CardException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


/**
 * @api {get} /api/token/status Obtener el estado del Token
 * @apiName TokenStatus
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Información de los tokens detectados
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "datos": {
 *         "connected": true,
 *         "tokens": [
 *             "FT ePass2003Auto"
 *         ]
 *     },
 *     "finalizado": true,
 *     "mensaje": "Lista de Tokens obtenida"
 * }
 * 
 * @apiSampleRequest https://localhost:9000/api/token/status
 */
public class RestTokenConectado extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonResponse = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            Pkcs11Scan pkcs11Scan = new Pkcs11Scan();
            for(String marca: pkcs11Scan.getListConnected()){
                jsonArray.add(marca);
            }
            jsonResponse.put("connected",jsonArray.size() > 0);    
            jsonResponse.put("tokens",jsonArray);
            writeResponse(true, "Lista de Tokens obtenida", jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException | CardException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}