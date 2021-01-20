package gob.adsib.fido.server.end_points;

import fidomoduleabstract.BaseEndPointServlet;
import fidomoduleabstract.HttpExchangeUtil;
import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.error.DeviceNoConnected;
import gob.adsib.fido.error.IncorrectPinException;
import gob.adsib.fido.error.LockedDeviceExcepcion;
import gob.adsib.fido.util.CertificateData;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @api {post} /api/token/data Obtener datos del token
 * @apiName TokenData
 * @apiGroup 2 Token
 * @apiVersion 1.3.0
 *
 * @apiSuccess {Json} datos Información del token
 * @apiSuccess {Boolean} finalizado Respuesta si el proceso interno finalizo
 * @apiSuccess {String} mensaje de salida del servicio
 * 
 * @apiParam {String} pin Pin del token.
 * 
 * @apiParamExample {json} Request-Example:
 * {
 *     "pin": "2121F21",
 *     "slot": 0
 * }
 * 
 * @apiSuccessExample Success-Response:
 *	HTTP/1.1 200 OK
 * {
 *    "datos":{
 *       "data_token":{
 *          "certificates":1,
 *          "data":[
 *             {
 *                "tipo":"PRIMARY_KEY",
 *                "tipo_desc":"Clave Privada",
 *                "alias":"35401721737428",
 *                "id":"35401721737428",
 *                "tiene_certificado":true
 *             },
 *             {
 *                "tipo":"X509_CERTIFICATE",
 *                "serialNumber":"230344d26fe3e391",
 *                "tipo_desc":"Certificado",
 *                "alias":"35401721737428",
 *                "pem":"-----BEGIN CERTIFICATE-----\nMIIHPDCCBSSgAwIBAgIIIwNE0m\/j45EwDQYJKoZIhvcNAQELBQAwSzEsMCoGA1UE\nAwwjRW50aWRhZCBDZXJ0aWZpY2Fkb3JhIFB1YmxpY2EgQURTSUIxDjAMBgNVBAoMBUFE\nU0lCMQswCQYDVQQGEwJCTzAeFw0xOTEwMTUxNjA5MDBaFw0yMDA4MTkyMzExMDBaMIHx\nMQswCQYDVQQuEwJDSTEaMBgGA1UEAwwRQUxWQVJPIEFQQVpBIFJVSVoxEjAQBgNVBAUT\nCTEyMDQzMTAyMDErMCkGA1UEDAwiUFJPRkVTSU9OQUwgRU4gQ0FMSURBRCBERSBTT0ZU\nV0FSRTEqMCgGA1UECwwhVU5JREFEIERFIElOTk9WQUNJT04gWSBERVNBUlJPTExPMQ4w\nDAYDVQQKDAVBRFNJQjELMAkGA1UEBhMCQk8xFDASBgcrBgEBAQEADAc2MTUwNTY0MSYw\nJAYDVQQNDB1QZXJzb25hIEp1cmlkaWNhIEZpcm1hIFNpbXBsZTCCASIwDQYJKoZIhvcN\nAQEBBQADggEPADCCAQoCggEBAMfg28Ngt8d9K4c\/Whxy7oju\/KVhK0yLGu4YpdZqZEfM\nOS6tSrDLWZazrVTw12glanBdk6VJHkJFQYrvjteOF8+9WnqCejv1zCmRE6Z\/F89stnJU\nI9AIhSvytltK4ln6l9MtSHuRFrX4rgq4VbLti7NgYjjjbIGsOEhnX28xq1kkEJV027vE\nS+KnNyCn1eGck9OlsjTgEDYEvk354pSFp3TwUYqDKV0vbSoZml\/+k\/ibScMjjf+Uxsv8\n5mVl5dlXbENggTKSKI6WivyiUOOd9BQMhH+x3zCHni9czkGRVkSkPTCUBXhKTtSALZs4\nhZsNsBCYK+r54w4pNHxvTE5x3dsCAwEAAaOCAnswggJ3MHkGCCsGAQUFBwEBBG0wazA7\nBggrBgEFBQcwAoYvaHR0cHM6Ly93d3cuZmlybWFkaWdpdGFsLmJvL2Zpcm1hZGlnaXRh\nbF9iby5wZW0wLAYIKwYBBQUHMAGGIGh0dHA6Ly93d3cuZmlybWFkaWdpdGFsLmJvL29j\nc3AvMB0GA1UdDgQWBBTDWCv+amusdrF+Jke+omE6oLTFlzAJBgNVHRMEAjAAMB8GA1Ud\nIwQYMBaAFNKZ3cFvJS4nqAvr3NnWkltiVaDCMIG8BgNVHSAEgbQwgbEwUAYOYEQAAAAB\nDgECAAEAAAAwPjA8BggrBgEFBQcCARYwaHR0cHM6Ly93d3cuZmlybWFkaWdpdGFsLmJv\nL3BvbGl0aWNhanVyaWRpY2EucGRmMF0GD2BEAAAAAQ4BAgABAgEAADBKMEgGCCsGAQUF\nBwICMDweOgBQAGUAcgBzAG8AbgBhACAASgB1AHIAaQBkAGkAYwBhACAARgBpAHIAbQBh\nACAAUwBpAG0AcABsAGUwgZMGA1UdHwSBizCBiDCBhaAyoDCGLmh0dHA6Ly93d3cuZmly\nbWFkaWdpdGFsLmJvL2Zpcm1hZGlnaXRhbF9iby5jcmyiT6RNMEsxLDAqBgNVBAMMI0Vu\ndGlkYWQgQ2VydGlmaWNhZG9yYSBQdWJsaWNhIEFEU0lCMQ4wDAYDVQQKDAVBRFNJQjEL\nMAkGA1UEBhMCQk8wCwYDVR0PBAQDAgTwMCcGA1UdJQQgMB4GCCsGAQUFBwMCBggrBgEF\nBQcDAwYIKwYBBQUHAwQwJAYDVR0RBB0wG4EZYWx2YXJvYXBhemFydWl6QGdtYWlsLmNv\nbTANBgkqhkiG9w0BAQsFAAOCAgEAnk4cfjkedplrqFW64sZdpzp3nMsTNvp4BpT6DPl8\n\/Nz24L2YcSOz+ritsn8kgaLNE1it19NL8fVVl5lCOKXYFAJAUh1WkaawZ3QzKnKAc+\/i\nD2ng4dtxovK0y\/m729Nwfhn8RzmJ+f9Fzra4T6jiOCrTmpk\/vSMRp+mMccOpyzebbhnD\nA8BnAL1ZHma+Ezu9oWdQXZZPSWRjxE+mesfPdP44GFt7JhwMHYmfn7I\/lzVHZlLDUXAy\no2HigfGg6oyMyK4+6IHRJr\/5\/+BGJWVZXRm7+fAdTyLxheYHTBabUbebMxSD8hclNbBq\nB5PMbHORBqcS2F9BrNdoyL8ktIEN7yjOHX4gUuk1NvzosprnqwYZq4RCofW5aFA\/JYZY\nWJC4Zn0vNSqb03aP95iZb6vbhu4daDu5R3ISIZcsRiLawe78+F6o\/oIeJY4o5qlDsHOf\nv+hnylUMHxrcOoj5bblQFJ5GUbeBhNLu4tkCVsiN8giJOKCEMeQ6kkekPj7JQeM+ia7N\n3EV6mcgB6jxOYo4d\/ncQbMGYk9gkKEVGeVKedgEdkRqHFmuDFSeWEYmEjhtCr2HZm6WX\nCmmsVpAonI7KH9oT1tK2MOKy2J0lJhuwlRgymZ7ID1ahI1wYeMBK30PEIcLmMlbp3TuS\nY3GoXF\/qCGBZq+\/ArjYyowcloXD0kAI=\n-----END CERTIFICATE-----",
 *                "validez":{
 *                   "hasta":"2019-10-15 12:09:00",
 *                   "desde":"2020-08-19 07:11:00"
 *                },
 *                "id":"35401721737428",
 *                "common_name":"ALVARO APAZA RUIZ",
 *                "titular":{
 *                   "UID":null,
 *                   "C":"BO",
 *                   "T":"PROFESIONAL EN CALIDAD DE SOFTWARE",
 *                   "dnQualifier":"CI",
 *                   "OU":"UNIDAD DE INNOVACION Y DESARROLLO",
 *                   "uidNumber":"6150564",
 *                   "description":"Persona Juridica Firma Simple",
 *                   "CN":"ALVARO APAZA RUIZ",
 *                   "EmailAddress":null,
 *                   "O":"ADSIB"
 *                },
 *                "emisor":{
 *                   "CN":"Entidad Certificadora Publica ADSIB",
 *                   "O":"ADSIB"
 *                },
 *                "resumenRevocacion": "REVOCADO",
 *                "detalleRevocacion": "El certificado no se encuentra revocado",
 *                "revocacion":{
 *                   "estado":"REVOKED",
 *                   "fecha":"2020-08-19 07:11:00",
 *                   "razon":"keyCompromise",
 *                }
 *             }
 *          ],
 *          "private_keys":1
 *       }
 *    },
 *    "finalizado":true,
 *    "mensaje":"Datos de token obtenidos correctamente"
 * }
 * 
 * @apiError (Error 400) {Object} datos Retorna <code>null</code> el el caso de error.
 * @apiError (Error 400) {Boolean} finalizado Retorna <code>false</code> el el caso de error.
 * @apiError (Error 400) {String} mensaje Descripción del error encontrado.
 * 
 * @apiErrorExample Error-Response:
 *	HTTP/1.1 400 Bad Request
    {
    	"datos": null,
    	"finalizado": false,
    	"mensaje": "Descripción del error"
    }
 */
public class RestObtenerDatosToken extends BaseEndPointServlet {
    @Override
    protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = null;
            if(httpExchange.getRequestMethod().equals("GET")){
                jsonRequest = new JSONObject();
                jsonRequest.put("pin",parametros.get("pin"));
            }else{
                jsonRequest = getRequestJSON(httpExchange);
            }
            System.out.println("JSON REQUEST TOKEN DATA: ");
            System.out.println(jsonRequest.toJSONString());
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
            String pin = jsonRequest.getAsString("pin");
            int slot = Integer.parseInt(jsonRequest.getAsString("slot"));
            
            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            
            List<String> listAlias = baseKeyStore.getAlieces();
            int countCertificates = 0;
            int countPrivateKeys = 0;
            
            JSONObject jsonDataToken = new JSONObject();
            JSONArray jsonData = new JSONArray();
            for (String alias: listAlias) {
                KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
                countPrivateKeys++;
                // Clave Privada
                JSONObject jsonClavePrivada = new JSONObject();
                jsonClavePrivada.put("tipo","PRIMARY_KEY");
                jsonClavePrivada.put("tipo_desc","Clave Privada");
                jsonClavePrivada.put("alias",keyAndCertificate.getAlias());
                jsonClavePrivada.put("id",keyAndCertificate.getAlias());
                jsonClavePrivada.put("tiene_certificado",!keyAndCertificate.isSeftSigned());
                
                jsonData.add(jsonClavePrivada);
                
                if(!keyAndCertificate.isSeftSigned()){
                    countCertificates++;
                    JSONObject jsonCertificado = new JSONObject();
                    
                    jsonCertificado.put("tipo","X509_CERTIFICATE");
                    jsonCertificado.put("tipo_desc","Certificado");
                    jsonCertificado.put("alias",keyAndCertificate.getAlias());
                    jsonCertificado.put("id",keyAndCertificate.getAlias());
                    jsonCertificado.put("adsib", keyAndCertificate.isAdsibSigned());
                    jsonCertificado.put("pem",keyAndCertificate.toPemCertificate());
                    keyAndCertificate.getCertificateData().writeInfo(jsonCertificado);
                    
                    // Verificación de la revocacion

                    try {
                        CertificateData certificateDataUser = new CertificateData(keyAndCertificate.getX509Certificate());
                        CertificateData certificateDataIssuer = new CertificateData(new File(Config.getConfig().getPathCertificadoEmisor()));
                        CertificateData.CertOcspStatus certificateStatus = certificateDataUser.verifRevocationWithOCSP(certificateDataIssuer.getFirstCertificate());
                        
                        JSONObject jsonRevocacion = new JSONObject();
                        switch(certificateStatus.status){
                            case "REVOKED": 
                                jsonCertificado.put("resumenRevocacion","REVOCADO");
                                jsonCertificado.put("detalleRevocacion","El certificado se encuentra revocado");
                                jsonRevocacion.put("estado",certificateStatus.status);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                jsonRevocacion.put("fecha", simpleDateFormat.format(certificateStatus.revocationTime));
                                jsonRevocacion.put("razon",certificateStatus.revocationReason);
                                jsonCertificado.put("revocacion",jsonRevocacion);
                                break;
                            case "GOOD": 
                                jsonCertificado.put("resumenRevocacion","CORRECTO");
                                jsonCertificado.put("detalleRevocacion","El certificado no se encuentra revocado");
                                jsonRevocacion.put("estado",certificateStatus.status);
                                jsonCertificado.put("revocacion",jsonRevocacion);
                                break;
                            case "UNKNOWN": 
                                jsonCertificado.put("resumenRevocacion","DESCONOCIDO");
                                jsonCertificado.put("detalleRevocacion","El certificado no se encuentra registrado en el OCSP");
                                jsonRevocacion.put("estado",certificateStatus.status);
                                jsonCertificado.put("revocacion",jsonRevocacion);
                                break;
                        }
                    } catch (IOException | CertificateException e) {
                        e.printStackTrace();
                        jsonCertificado.put("resumenRevocacion","NO SE PUDO VERIFICAR");
                        jsonCertificado.put("detalleRevocacion",e.getMessage());
                        jsonCertificado.put("revocacion",null);
                    }
                    
                    jsonData.add(jsonCertificado);                    
                }
            }
            jsonDataToken.put("certificates",countCertificates);
            jsonDataToken.put("private_keys",countPrivateKeys);
            jsonDataToken.put("data", jsonData);
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("data_token",jsonDataToken);
            
            baseKeyStore.logout();
            
            // Escribiendo respuesta
            writeResponse(true, "Datos de token obtenidos correctamente",jsonResponse,HttpURLConnection.HTTP_OK, httpExchange);
            
        } catch (IncorrectPinException e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorCode", "PIN_INCORRECTO");
            writeError(e, httpExchange,jsonObject);
        } catch (DeviceNoConnected e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorCode", "DEVICE_NO_CONNECTED");
            writeError(e, httpExchange,jsonObject);
        } catch (LockedDeviceExcepcion e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorCode", "DEVICE_LOCKED");
            writeError(e, httpExchange,jsonObject);
        } catch(Exception e){
            writeErrorResponse(e, httpExchange);
        }
        finally{
            httpExchange.close();
        }
    }
}