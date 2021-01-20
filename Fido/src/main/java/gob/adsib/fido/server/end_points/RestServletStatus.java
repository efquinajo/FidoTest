package gob.adsib.fido.server.end_points;

import java.net.HttpURLConnection;
import java.util.HashMap;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import net.minidev.json.JSONObject;

/**
 * @api {get} https://localhost:9000/api/status Verifica estado de token
 * @apiName Estado de Servicio
 * @apiGroup Servicio
 * @apiVersion 1.3.0
 *
 * @apiSuccessExample Success-Response:
 *	HTTP/1.1 200 OK
    {
       "datos":{
          "ultima_version_api_info":{
             "url_descarga_ultima_version":"https://firmadigital.bo/herramientas/#descargas",
             "compilacion":1020,
             "api_version":"1.0.2"
          },
          "compilacion":1020,
          "api_version":"1.0.2"
       },
       "finalizado":true,
       "mensaje":"Servicio iniciado correctamente"
    }
 * 
 * 
 * @apiSuccess {Json} datos Para esta petición retorna un json vacio {}
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 */
public class RestServletStatus extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("compilacion",3100);
            jsonData.put("api_version","1.0.2");
            
            writeResponse(true, "Servicio ejecutandose correctamente", jsonData, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}