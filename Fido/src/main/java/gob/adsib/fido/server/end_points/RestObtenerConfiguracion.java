package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import net.minidev.json.JSONObject;

/**
 * @api {get} /api/config/obtener Obtener configuracion
 * @apiName TokenCargarPEM
 * @apiGroup Configuracion
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Para esta petición retorna un json vacio {}
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiSuccessExample Configuració:
 * HTTP/1.1 200 OK
 * {
 *     "datos": {
 *          "certificado.emisor": "...",
 *          "url.crl": "...",
 *          "profile.file_name": "Profile Data",
 *          "profile.name": "Data",
 *          "profile": {
 *               ""
 *           }
 *     },
 *     "finalizado": true, 
 *     "mensaje": "Parámetros para archivo de configuracion"
 * }
 */
public class RestObtenerConfiguracion extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            Config config = Config.getConfig();
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("certificado.emisor",config.getPathCertificadoEmisor());
            Profile profile = Profile.getDefaultProfile();
            jsonResponse.put("profile.file_name",profile==null?null:profile.getFileName().getAbsolutePath());
            jsonResponse.put("profile.name",profile==null?null:profile.getFileName().getName());
            jsonResponse.put("firmatic.enabled",String.valueOf(config.isEnabledFirmaticServiceOnFido()));

            if(profile!=null){
                JSONObject jsonProfileData = new JSONObject();
                profile.writeDate(jsonProfileData);
                jsonResponse.put("profile",jsonProfileData);
            }

            writeResponse(true, "Parámetros para archivo de configuracion",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
            
        } catch (IOException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}