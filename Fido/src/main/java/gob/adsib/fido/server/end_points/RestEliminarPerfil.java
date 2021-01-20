package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Profile;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import net.minidev.json.JSONObject;

/**
 * @api {delete} /api/profile/delete?profileName=Profile Elimina un perfile de usuario
 * @apiName TokenInfo
 * @apiGroup Profile
 * @apiVersion 1.3.0
 *
 * @apiParam {String} profileName El nombre del perfil a eliminar.
 * 
 * @apiSuccess {Json} datos Perfil eliminado
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *      "mensaje": "Perfil eliminado correctamente"
 * }
 * 
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 * 
 * @apiErrorExample Error-Response:
 * HTTP/1.1 400 Bad Request
 * {
 *     "datos": null,
 *     "finalizado": false,
 *     "mensaje": "Descripción del error"
 * }
 */
public class RestEliminarPerfil extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            List<Profile> listProfiles = Profile.getListProfile();
            Profile profileDelete = null;
            for (Profile profile : listProfiles) {
                if(profile.getName().equals(parametros.get("profileName"))){
                    profileDelete = profile;
                    break;
                }
            }
            if(profileDelete==null)
                throw new IOException("No se encontro le perfil seleccionado");
            if(profileDelete.isDefaultProfile() && listProfiles.size()>1)
                throw new IOException("No se puede eliminar el perfil seleccionado");
            boolean deleted = profileDelete.getFileName().delete();
            if(!deleted)
                throw new IOException("No se pudo eliminar el archivo ["+profileDelete.getFileName()+"]");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("profileSelected",profileDelete.isDefaultProfile());
            jsonResponse.put("numberOfProfiles",listProfiles.size()-1);
            writeResponse(true, "Perfil eliminado correctamente",jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}