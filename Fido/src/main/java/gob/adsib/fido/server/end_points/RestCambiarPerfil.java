package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Profile;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;

/**
 * @api {put} /api/profile/change Cambiar de perfil
 * @apiName TokenInfo
 * @apiGroup Profile
 * @apiParam {String} selectProfileName El nombre del perfil al que sera cambiado
 * @apiVersion 1.3.0
 * 
 * @apiParamExample {json} Cambiar Perfil: 
 * {
 *    "selectProfileName": "Perfil"
 * }
 *
 * @apiSuccess {Json} datos Información del token
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiSuccessExample Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "finalizado": true,
 *     "mensaje": "Perfile cambiado correctamnete",
 *     "datos": null
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
public class RestCambiarPerfil extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            String profileName = jsonRequest.getAsString("selectProfileName");
            List<Profile> listProfiles = Profile.getListProfile();
            Profile profileSelected = null;
            Profile profileToChange = null;
            for (Profile profile : listProfiles) {
                if(profile.getName().equals(profileName)){
                    profileToChange = profile;
                }
                if(profile.isDefaultProfile()){
                    profileSelected = profile;
                }
            }
            if(profileToChange==null){
                throw new IOException(String.format("No se encontro el perfile con nombre [%s]", profileName));
            }
            // Eliminamos el perfil seleccionado
            if(profileSelected!=null){
                Path temp = Files.move(
                        Paths.get(profileSelected.getFileName().getAbsolutePath()),
                        Paths.get(profileSelected.getFileName().getAbsolutePath().replaceAll(".default",""))); 

                if(temp != null){ 
                    System.out.println("Perfil por defecto movido correctamente"); 
                } 
                else{ 
                    System.out.println("No se pudo mover archivo"); 
                }
            }
            
            // Seleccionamos el nuevo perfil
            Path temp = Files.move(
                    Paths.get(profileToChange.getFileName().getAbsolutePath()),
                    Paths.get(profileToChange.getFileName().getAbsolutePath().replaceAll(".profile",".default.profile"))); 

            if(temp != null){ 
                System.out.println("Perfile seleccionado cambiado correctamente"); 
            } 
            else{ 
                System.out.println("No se pudo mover archivo"); 
            }
            
            writeResponse(true, "Perfil cambiado correctamnete", null, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException | ParseException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}