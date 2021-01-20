package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.Profile;
import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import gob.adsib.fido.stores.profiles.Pkcs12Profile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;

/**
 * @api {post} /api/profile/crear Crear un nuevo perfil
 * @apiDescription Crea un nuevo perfil para uso de par de claves y certificados.
 * @apiName Crear perfil
 * @apiParam {String} name El nombre del perfil con el que sera creado
 * @apiParam {String} type El tipo de keystore asociado al perfil PKCS11 o PKCS12 (PKCS#12 no esta actualmente soportado)
 * @apiParam {String} driverPath La ruta absoluta del controlador
 * 
 * @apiGroup Profile
 * @apiVersion 1.3.0
 * 
 * @apiParamExample {json} Crear Perfil TOKEN:
 * {
 *     "name": "Nombre de perfil",
 *     "type": "PKCS11",
 *     "driverPath": "/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so"
 * }
 * 
 * @apiParamExample {json} Crear Perfil SOFTWARE:
 * {
 *     "name": "Nombre de perfil Software",
 *     "type": "PKCS12",
 *     "fileP12Path": "/home/documentos/claves.p12"
 * }
 *
 * @apiSuccess {Json} datos Un Json que contiene el resultado de la creación.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiSuccessExample Perfil Creado: 
 * HTTP/1.1 200 OK 
 * { 
 *     "datos": {
 *        "created": true
 *      },
 *     "finalizado": true,
 *     "mensaje": "Perfil creado correactamente"
 * }
 *
 * @apiError (Error 409) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 409) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 409) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: 
 * HTTP/1.1 409 Conflict
 * {
 *      "datos": null,
 *      "finalizado": false,
 *      "mensaje": "Mensaje del porque no se pudo obtener lainfo"
 * }
 */
public class RestCrearPerfil extends BaseEndPointServlet {

    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            String name = jsonRequest.getAsString("name");
            String type = jsonRequest.getAsString("type");
            String driverPath = jsonRequest.getAsString("driverPath");
            String fileP12Path = jsonRequest.getAsString("fileP12Path");
            
            // Obtenemos perfil por defecto
            Profile profileDefault = Profile.getDefaultProfile();
            
            // Creamos el perfil
            if(profileDefault!=null){
                Path temp = Files.move(
                        Paths.get(profileDefault.getFileName().getAbsolutePath()),
                        Paths.get(profileDefault.getFileName().getAbsolutePath().replaceAll(".profile",".default.profile"))); 

                if(temp != null){ 
                    System.out.println("Perfile seleccionado cambiado correctamente"); 
                } 
                else{ 
                    System.out.println("No se pudo mover archivo"); 
                }
            }
            
            File fileProfile = Profile.createFileProfile(type);
            
            fileProfile.createNewFile();
            Profile profile = null;
            
            if(Profile.TYPE_PKCS11.equals(type)){
                profile = Pkcs11Profile.newInstance(fileProfile, name, new File(driverPath), -1);
            }else if(Profile.TYPE_PKCS12.equals(type)){
                profile = Pkcs12Profile.newInstance(fileProfile, name, new File(fileP12Path));
            }else
                throw new IOException("Tipo de KEY STORE invalido ["+type+"]");
            
            // Establecemos en nuevo perfil como por defecto
                        // Seleccionamos el nuevo perfil
            
            // Guardamos el perfil
            profile.save();
            
            // Actualizamos la configuración como perfil por defecto
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("fileName", profile.getFileName().getAbsolutePath());
            
            writeResponse(true, "Perfil creado correactamente",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException | ParseException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}