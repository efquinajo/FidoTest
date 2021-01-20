package gob.adsib.fido.server.end_points_firmatic;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.util.IOUtil;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.net.HttpURLConnection;

/**
 * @api {post} https://localhost:4637/ Home Firmatic
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
 * Un sitio 

 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK 
 * {
 *     "files":[{
 *         "base64":"JVBERi0xLjQKM",
 *         "name":"22949.pdf"
 *     }]
 * }
 */
public class RestHomeFirmatic extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        System.out.println("HOME FIRMATIC");        
        try {
            InputStream inputHTML = RestHomeFirmatic.class.getResourceAsStream("/firmatic.html");
            byte[] htmlFile = IOUtil.inputStreamToBytes(inputHTML);
            httpExchange.getResponse().setHeader("Content-Type", "text/html; charset=utf-8");
            httpExchange.getResponse().setHeader("Access-Control-Allow-Origin", "*");
            httpExchange.getResponse().setHeader("Connection", "keep-alive");
            httpExchange.getResponse().setHeader("X-Powered-By", "Express"); // valido solo para firmatic

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, htmlFile.length);
            httpExchange.getResponseBody().write(htmlFile);
            httpExchange.getResponseBody().flush();

        } catch (IOException e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}