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
 * @Autor: equinajo
 * @Date : 16/1/2021
 * EDV
 */
public class RestFirmarPdfStampToken extends BaseEndPointServlet {
    @Override
    protected synchronized void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros){
        try {
            JSONObject jsonRequest = null;
            if(httpExchange.getRequestMethod().equals("GET")){
                jsonRequest = new JSONObject();
                jsonRequest.put("pin",parametros.get("pin"));
                jsonRequest.put("alias",parametros.get("alias"));
                jsonRequest.put("pdf",parametros.get("pdf"));
                //Obteniendo parametros para graficar la firma
                jsonRequest.put("coorX0",parametros.get("coorX0"));
                jsonRequest.put("coorY0",parametros.get("coorY0"));
                jsonRequest.put("coorX1",parametros.get("coorX1"));
                jsonRequest.put("coorY1",parametros.get("coorY1"));
                jsonRequest.put("fieldName",parametros.get("fieldName"));
                jsonRequest.put("reason",parametros.get("reason"));
                jsonRequest.put("page",parametros.get("page"));
                jsonRequest.put("location",parametros.get("location"));
            }else{
                jsonRequest = getRequestJSON(httpExchange);
            }
            if(!jsonRequest.containsKey("slot"))
                jsonRequest.appendField("slot",Config.DEFAULT_SLOT);
            String pdf = jsonRequest.getAsString("pdf");
            String pin = jsonRequest.getAsString("pin");
            long slot = Long.parseLong(jsonRequest.getAsString("slot"));
            String alias = jsonRequest.getAsString("alias");
            //Obteniendo parametros para graficar la firma
            int coorX0=Integer.parseInt(jsonRequest.getAsString("coorX0"));
            int coorY0=Integer.parseInt(jsonRequest.getAsString("coorY0"));
            int coorX1=Integer.parseInt(jsonRequest.getAsString("coorX1"));
            int coorY1=Integer.parseInt(jsonRequest.getAsString("coorY1"));
            String fieldName = jsonRequest.getAsString("fieldName");
            String reason = jsonRequest.getAsString("reason");
            int page=Integer.parseInt(jsonRequest.getAsString("page"));
            String location = jsonRequest.getAsString("location");
            Profile profile = Profile.getDefaultProfile();
            BaseKeyStore baseKeyStore = profile.getKeyStore();
            baseKeyStore.login(pin,slot);
            KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
            byte[] pdfSigned = keyAndCertificate.signPdfStamp(Base64.decode(pdf),coorX0,coorY0,coorX1,coorY1,fieldName,reason,page,location);
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