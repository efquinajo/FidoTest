package gob.adsib.fido.server.end_points;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.StringTokenizer;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {get} /api/usbdisk/serial Obtener Serial del USBDisk
 * @apiDescription <strong>Nota.-</strong> Esta solicitud solo es compatible en
 * el sistema Operativo <strong>Linux</strong>.
 * @apiName UsbDiskSerial
 * @apiGroup 4 UsbDisk Linux
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Un Json que contiene el vector de usbs detectadas en
 * el equipo.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK 
 * {
 *     "datos": {
 *         "usbs": [{
 *             "serial": "AA00000000000755",
 *             "name": "sdc",
 *             "type": "disk",
 *             "tran": "usb"
 *         }] }, 
 *     "finalizado": true,
 *     "mensaje": "Operación finalizada correctamente..."
 * }
 *
 * @apiError (Error 409) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 409) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 409) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 409 Conflict
 * {
 *    "datos": null,
 *    "finalizado": false,
 *    "mensaje": "Mensaje del porque no se pudo obtener la info"
 * }
 *
 * @apiSampleRequest https://localhost:9000/api/usbdisk/serial
 */
public class RestUsbDiskSerial extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            // Solo para Linux
            Process p;
            final String COMANDO = "lsblk --nodeps -o name,serial,type,tran";
            System.out.println(COMANDO);
            System.out.println("Obteniendo la información del usb...");
            p = Runtime.getRuntime().exec(COMANDO);
            // p.destroy();
            JSONArray jsonArrayUsb = new JSONArray();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String linea = null;
            while ((linea = in.readLine()) != null) {
                StringTokenizer stoken = new StringTokenizer(linea);
                if (stoken.countTokens() >= 4 && !linea.replace(" ", "").equals("NAMESERIALTYPETRAN")) {
                    JSONObject jsonUsb = new JSONObject();
                    int ind = 0;
                    while (stoken.hasMoreTokens()) {
                        String tk = stoken.nextToken();
                        // System.out.println("--> "+ tk);
                        switch (ind) {
                            case 0:
                                jsonUsb.put("name", tk);
                                break;
                            case 1:
                                jsonUsb.put("serial", tk);
                                break;
                            case 2:
                                jsonUsb.put("type", tk);
                                break;
                            case 3:
                                jsonUsb.put("tran", tk);
                                break;
                        }
                        ind++;
                    }
                    if (jsonUsb.get("type").equals("disk") && jsonUsb.get("tran").equals("usb")) {
                        jsonArrayUsb.add(jsonUsb);
                    }
                }
            }
            in.close();
            System.out.println("OK");
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("usbs", jsonArrayUsb);
            writeResponse(true, "Operación finalizada correctamente...", jsonResponse, HttpURLConnection.HTTP_OK, httpExchange);
        } catch (IOException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}