package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.controller.ValidarFirmaMultiplesPdfs;
import gob.adsib.fido.util.PdfSignatures;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;
import java.util.HashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;

/**
 * @api {post} /api/validar_firma_multiples_pdfs Validar las firmas Documentos PDF
 * @apiName ServicioValidarFirmarMulPdfs
 * @apiGroup Servicio
 * @apiVersion 1.3.0
 *
 * @apiParam {array} pdf El archivo PDF a verficar en formato Base64.
 *
 * @apiSuccess {object} datos Un conjunto de información sobre la firma del documento.
 * @apiSuccess {boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {string} mensaje Mensaje de salida del servicio.
 *
 * @apiParamExample {json} Validar PDFs
 * {
 *    "documentos": [
 *       {  "pdfBase64": "JVBERi0x...(BASE64)...LjMKJRgo=", "codigo": "1"},
 *       {  "pdfBase64": "JVBERi0x...(BASE64)...LjMKJRgo=", "codigo": "2"}
 *    ]
 * }
 *
 * @apiSuccessExample Firmas del documento PDF: 
 * HTTP/1.1 200 OK 
 * {
 *     "datos":[{
 *          "codigo": "1",
 *          "resultado": {
 *               "cadena_de_confianza": true,
 *               "modificado": false,
 *               "revocado": false,
 *               "fecha_sistema": "2017-07-14 12:02:01",
 *               "firmas": [ {
 *                    "fecha_firma": "2017-07-14 12:02:01",
 *                    "resumenRevocacion": "REVOCADO",
 *                    "detalleRevocacion": "El certificado no se encuentra revocado",
 *                    "revocado": true,
 *                    "detalle_revocado": {
 *                          "fecha": "2017-10-13 15:45:02",
 *                          "razon": "keyCompromise"
 *                    },
 *                    "cadena_de_confianza": true,
 *                    "documento_modificado": false,
 *                    "verificacion_revocado": "online",
 *                    "para_todo_el_documento": true,
 *                    "firma": "Signature1",
 *                    "certificado": {
 *                         "estado_fecha_firma": "valido",
 *                         "estado_actual": "expirado",
 *                         "pem": "-----BEGIN CERTIFICATE-----\n MIIGhDC...21F...jN3qZw==\n -----END CERTIFICATE-----\n",
 *                         "vigente": false,
 *                         "validez": {
 *                              "hasta": "2018-04-28 19:31:49.00",
 *                              "desde": "2017-06-30 19:31:49.00"
 *                         },
 *                         "titular": {
 *                              "UID": "1H",
 *                              "C": "BO",
 *                              "T": "Director Ejecutivo",
 *                              "OU": "Dirección",
 *                              "uidNumber": "12735834",
 *                              "DN": "CI",
 *                              "CN": "Sylvain Damien Lesage",
 *                              "O": "ADSIB"
 *                         },
 *                         "emisor": {
 *                              "C": "BO",
 *                              "CN": "Entidad Certificadora Publica ADSIB",
 *                              "O": "ADSIB"
 *                         }
 *                    }
 *              }],
 *             "firmado": true
 *         }
 *     }],
 *     "finalizado": true,
 *     "mensaje": "Sin errores!"
 * }
 *
 * @apiError (Error 400) {object} datos Retorna <code>null</code> en caso de error.
 * @apiError (Error 400) {boolean} finalizado Retorna <code>false</code> el caso de error.
 * @apiError (Error 400) {string} mensaje Descripción del error encontrado.
 *
 * @apiErrorExample Error-Response: 
 *   HTTP/1.1 400 Bad Request
 * {
 *     "datos": null,
 *     "finalizado": false,
 *     "mensaje": "Descripción del error"
 * }
 */
public class RestValidarFirmaMultiplesPdfs extends BaseEndPointServlet {

    @SuppressWarnings({"unchecked"})
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        // para peticiones POST
        if (!httpExchange.getRequestMethod().equals("POST")) {
            writeErrorResponse(new IOException("Solo se permite peticiones POST"), httpExchange);
            httpExchange.close();
            return;
        }
        // obtenemos los valores json
        try {
            JSONObject jsonRequest = getRequestJSON(httpExchange);
            JSONArray jsonArrayResult = ValidarFirmaMultiplesPdfs.validarDocumetosPDF(jsonRequest);
                    
            writeResponse(true, "Sin errores!", jsonArrayResult, HttpURLConnection.HTTP_OK, httpExchange);
            
        } catch (ParseException |CertificateException ex) {
            writeErrorResponse( new IOException("El contenido enviado para el servicio no es un JSON"), httpExchange);
        } catch (IOException ex) {
            writeErrorResponse(ex, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}