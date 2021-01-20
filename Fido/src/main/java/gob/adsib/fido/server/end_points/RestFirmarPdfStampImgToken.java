package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import net.minidev.json.JSONObject;
import org.bouncycastle.util.encoders.Base64;

import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * @api {post} /api/token/firmar_pdf Firmar un documento PDF
 * @apiName TokenFirmarPDF
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiParam {String} pin El pin del token.
 * @apiParam {String} pdf El archivo PDF a firmar en formato Base64.
 * @apiParam {String} alias El Alias del certificado con el cual se firmara.
 * @apiParam {String} pdf El documento PDF en Base64
 *
 * @apiSuccess {Json} datos Un Json que contiene el Strig
 * <code>pdf_firmado</code> en formato base64.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Request-Example: 
 * {
 *    "pin": "12345678", 
 *    "slot": 0,
 *    "alias": "2222232121122",
 *    "pdf": "JVBERi0xLjMKJ...(BASE64)...PRgo=", "alias": "787738531"
 * }
 *
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK
 * {
 *     "datos": {
 *     "pdf_firmado": "eyJhbG...(BASE64)...pTgPCw" },
 *     "finalizado": true,
 *     "mensaje": "Se firmo el pdf correctamente!"
 * }
 *
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 400 Bad Request
 * {
 *     "datos": null,
 *     "finalizado": false,
 *     "mensaje": "Descripción del error"
 * }
 */
public class RestFirmarPdfStampImgToken extends BaseEndPointServlet {
    @Override
    protected synchronized void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = null;
            if(httpExchange.getRequestMethod().equals("GET")){
                jsonRequest = new JSONObject();
                jsonRequest.put("pin",parametros.get("pin"));
                jsonRequest.put("alias",parametros.get("alias"));
                jsonRequest.put("pdf",parametros.get("pdf"));

                jsonRequest.put("xOne",parametros.get("coorX0"));
                jsonRequest.put("yOne",parametros.get("coorY0"));
                jsonRequest.put("page",parametros.get("page"));
            }else{
                jsonRequest = getRequestJSON(httpExchange);
            }
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
            String pdf = jsonRequest.getAsString("pdf");
            String pin = jsonRequest.getAsString("pin");
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));
            String alias = jsonRequest.getAsString("alias");

            int xOne=Integer.parseInt(jsonRequest.getAsString("coorX0"));
            int yOne=Integer.parseInt(jsonRequest.getAsString("coorY0"));
            int page=Integer.parseInt(jsonRequest.getAsString("page"));

            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            byte[] pdfSigned = keyAndCertificate.signPdfStampImg(Base64.decode(pdf),xOne,yOne,page);
            baseKeyStore.logout();

            // Escribiendo respuesta
            String encodedString = new String(Base64.encode(pdfSigned));
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("pdf_firmado", encodedString);
            writeResponse(true, "Se firmo el pdf correctamente!",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}