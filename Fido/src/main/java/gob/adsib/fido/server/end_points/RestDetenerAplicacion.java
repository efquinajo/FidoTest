package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.AppFido;
import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * @api {get} /api/shutdown_service Detener el servicio hasta que se lo vuelva a reiniciar.
 * @apiDescription Esta solicitud detiene el servicio hasta que el monitor del mismo lo vuelva a ejecutar.
 * @apiName DetenerServicio
 * @apiGroup 1 Servicio
 * @apiVersion 1.3.0
 * 
 * @apiSuccess {Json} datos Para esta petición devuelve <code>null</code>.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 *
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *   "datos":null,
 *   "finalizado":true,
 *   "mensaje":"Se cerró el servicio correctamente"
 * }
 */

/**
 * GET http://localhost:8000/api/showdown_service
 */
public class RestDetenerAplicacion extends BaseEndPointServlet {
    @Override
    @SuppressWarnings({})
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            writeResponse(true,"Se cerró el servicio correctamente", null,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            httpExchange.close();
        }
        AppFido.stopServer();
        System.out.println("Deteniendo servicio de fido");
        System.exit(0);
    }
}