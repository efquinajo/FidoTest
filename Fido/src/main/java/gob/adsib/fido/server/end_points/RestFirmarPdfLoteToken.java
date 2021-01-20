package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.controller.FirmarPdfLoteToken;

import java.util.HashMap;
import java.net.HttpURLConnection;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/firmar_lote_pdfs Firmar lote de documentos PDF
 * @apiName TokenFirmarPdfLote
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiParam {String} pin El pin del token.
 * @apiParam {String} pdfs Array de documentos PDF a firmar con sus respectivos identificadores.
 * @apiParam {String} alias El Alias del certificado con el cual se firmara.
 * @apiParam {Array} pdfs El array de documentos PDF agrupados por id y pdf
 * @apiParam {String} pdfs.id El identificado único para la solicitud
 * @apiParam {String} pdfs.pdf El documento PDF en Base64
 *
 * @apiSuccess {Json} datos Un Json que contiene el Strig
 * <code>pdf_firmado</code> en formato base64.
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Request-Example: 
 * { 
 *      "pin": "12345678",
 *      "alias": "787738531",
 *      "slot": 0,
 *      "pdfs": [{
 *             "id": "documento0.pdf",
 *             "pdf": "JVBERi0xLjMKJ...(BASE64)...PRgo="
 *         },{ 
 *             "id": "documento1.pdf", 
 *             "pdf": "JVBERi0xLjMKJ...(BASE64)...PRgo="
 *         }, ... 
 *      ]
 *  }
 * @apiSuccessExample Success-Response: HTTP/1.1 200 OK 
 * { 
 *    "datos": {
 *        "pdfs_firmados": [{
 *                "id": "documento0.pdf",
 *                "pdf_firmado": "eyJhbG...(BASE64)...pTgPCw"
 *             },{
 *                "id": "documento1.pdf",
 *                "pdf_firmado": "eysJhbG...(BASE64)...pTgPCw"
 *             }, ...
 *         ],
 *         "tiempo_proceso": 9765 
 *    },
 *    "finalizado": true,
 *    "mensaje": "Archivos PDF firmados correctamente"
 * }
 *
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de
 * error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el
 * caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: HTTP/1.1 400 Bad Request 
 * {
 *     "datos": null,
 *     "finalizado": false,
 *     "mensaje": "Descripción del error"
 * }
 */
public class RestFirmarPdfLoteToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        System.out.println("FIRMADO DE PDF POR LOTE");
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            JSONArray jsonArrayPdfFirmados = FirmarPdfLoteToken.firmarLotePDF(jsonRequest);
            // Escribiendo respuesta
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("pdfs_firmados", jsonArrayPdfFirmados);
            writeResponse(true, "Archivos PDF firmados correctamente",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
        } catch (Exception e) {
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}
