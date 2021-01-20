package gob.adsib.fido.controller;

import gob.adsib.fido.util.PdfSignatures;
import java.io.IOException;
import java.security.cert.CertificateException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author UID-ADSIB
 */
public class ValidarFirmaMultiplesPdfs {
    public static JSONArray validarDocumetosPDF(JSONObject jsonRequest) throws IOException, CertificateException{
        JSONArray jsonArrayPdfs = (JSONArray)jsonRequest.get("documentos");
        JSONArray jsonArrayResult = new JSONArray();
        for (Object jsonObject : jsonArrayPdfs) {
            JSONObject jsonPdf = (JSONObject)jsonObject;
            if(!jsonPdf.containsKey("pdfBase64"))
                throw new IOException("No se envió el parámetro PDF para la solicitud");
            PdfSignatures pdfSignatures = new PdfSignatures(Base64.decodeBase64(jsonPdf.getAsString("pdfBase64")));
            JSONObject jsonSignatures = pdfSignatures.getJsonSignatures();

            JSONObject jsonPdfSigned = new JSONObject();
            jsonPdfSigned.put("codigo",jsonPdf.get("codigo"));
            jsonPdfSigned.put("resultado",jsonSignatures);

            jsonArrayResult.add(jsonPdfSigned);
        }
        return jsonArrayResult;
    }
}