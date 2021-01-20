package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Config;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * @api {get} https://localhost:9000/api/firmatic?enable=? Habilita o deshabilita el servicio de Firmatic
 * @apiName Habilitar o desabilitar Firmatic
 * @apiGroup Servicio
 * @apiVersion 1.3.0
 *
 * @apiSuccessExample Success-Response:
 * https://localhost:9000/api/firmatic?enable=true
 * 
 */
public class RestServletEnableDisableFirmatic extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            String enable = (String)parametros.get("enable");
            if(enable==null){
                writeErrorResponse(new IOException("Es obligatorio el parámetro en la URL el campo [enable]"), httpExchange);
                return;
            }
            boolean en = Boolean.parseBoolean(enable);
            Config.getConfig().setEnabledFirmaticService(en);
            System.out.println("GUARDNDO CAMBIOOOOOOOOOOOO");
            Config.getConfig().save();

            writeResponse(true, "Actualizado correctamente. Reinicie el servidor para hacer efectivo los cambios",null, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}