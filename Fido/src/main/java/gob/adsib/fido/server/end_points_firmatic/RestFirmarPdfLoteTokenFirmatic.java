package gob.adsib.fido.server.end_points_firmatic;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.profiles.Pkcs11Profile;


import java.io.IOException;
import java.util.HashMap;
import org.bouncycastle.util.encoders.Base64;
import java.net.HttpURLConnection;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {post} https://localhost:4637/sign Firmar lote de documentos PDF
 * @apiName TokenFirmarPdfLoteFirmatic
 * @apiGroup Firmatic
 * @apiVersion 3.0.0
 *
  * @apiParam {Array} archivo El array de documentos PDF agrupados por id y pdf
 * @apiParam {String} archivo.base64 El identificado único para la solicitud
 * @apiParam {String} archivo.name El documento PDF en Base64
 *
 * @apiSuccess {Json} datos Un Json que contiene el Strig
 * <code>pdf_firmado</code> en formato base64.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *     "archivo":[{
 *         "base64":"data:application/pdf;base64,JVBERi0xLjQKMSAwIG9iago8PAovVGl0b....",
 *         "name":"22949.pdf"
 *     }],
 *     "format":"pades",
 *     "language":"es",
 *     "ci":"6817702"
 * }

 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK 
 * {
 *     "files":[{
 *         "base64":"JVBERi0xLjQKM",
 *         "name":"22949.pdf"
 *     }]
 * }
 */
public class RestFirmarPdfLoteTokenFirmatic extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {}
        
        System.out.println("FIRMADO DE PDF POR LOTE FIRMATIC");
        
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            JSONArray jsonArrayPdf = (JSONArray)jsonRequest.get("archivo");
            
            System.out.println("Obteniendo configuración");
            Pkcs11Profile profile = (Pkcs11Profile)Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            System.out.println("Mostrando autenticación");
            DialogSessionToken dialogSessionToken = new DialogSessionToken(profile);
            dialogSessionToken.loadSlots();
            dialogSessionToken.setVisible(true);
            dialogSessionToken.setFocusable(true);
            dialogSessionToken.requestFocus();
            DialogSessionToken.DataToken dataToken = dialogSessionToken.waitToCloseWindows();
            
            if(dataToken == null){ // Firma Cancelada
                writeErrorResponse(new IOException("Error al firma o fue cancelada"), httpExchange);
                return;
            }
            String pin = dataToken.getPin();
            long slot = dataToken.getSlot();
            String alias = dataToken.getAlias();
            baseKeyStore.login(pin,slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            
            JSONArray jsonArrayPdfFirmados = new JSONArray();
            for (Object object : jsonArrayPdf) {
                JSONObject jsonPdf = (JSONObject) object;
                String pdf = jsonPdf.getAsString("base64");
                pdf = pdf.replace("data:application/pdf;base64,","");
                String id = jsonPdf.getAsString("name");
                
                byte[] pdfSigned = keyAndCertificate.signPdf(Base64.decode(pdf));
                
                JSONObject jsonPdfFirmado = new JSONObject();
                jsonPdfFirmado.put("name",id);
                jsonPdfFirmado.put("base64",new String(Base64.encode(pdfSigned)));
                jsonArrayPdfFirmados.add(jsonPdfFirmado);
            }
            
            baseKeyStore.logout();
            
            // Escribiendo respuesta
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("files",jsonArrayPdfFirmados);
            
            byte[] resp = String.valueOf(jsonResponse.toJSONString()).getBytes("UTF-8");
//            Headers responseHeaders = httpExchange.getResponseHeaders();

            httpExchange.getResponse().setHeader("Accept", "*/*" ); 
            httpExchange.getResponse().setHeader("Accept-Ranges", "bytes" );
            httpExchange.getResponse().setHeader("Access-Control-Allow-Origin", "*" ); 
    //        httpExchange.getResponse().setHeader("Access-Control-Credentials", "true" ); 
            httpExchange.getResponse().setHeader("Access-Control-Allow-Headers", "X-Requested-With");

            httpExchange.getResponse().setHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS" ); 
            httpExchange.getResponse().setHeader("Access-Control-Max-Age", "1000" );
            httpExchange.getResponse().setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpExchange.getResponse().setHeader("Pragma", "no-cache");
            httpExchange.getResponse().setHeader("Expires", "0");
            httpExchange.getResponse().setHeader("Content-Type", "application/json; charset=UTF-8");


            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
            httpExchange.getResponseBody().write(resp);
            httpExchange.getResponseBody().flush();
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}