/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.adsib.fido.controller;

import gob.adsib.fido.util.PKCS7Signatures;
import java.io.IOException;
import java.security.cert.CertificateException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author alberto
 */
public class ValidarFirmaMultiplesPKCS7 {
    public static JSONArray validarDocumetosPKCS7(JSONObject jsonRequest) throws IOException, CertificateException {
        JSONArray jsonArrayPdfs = (JSONArray)jsonRequest.get("documentos");
        JSONArray jsonArrayResult = new JSONArray();
        for (Object jsonObject : jsonArrayPdfs) {
            JSONObject jsonPdf = (JSONObject)jsonObject;
            if(!jsonPdf.containsKey("firma"))
                throw new IOException("No se envió el parámetro firma para la solicitud");
            PKCS7Signatures signatures = new PKCS7Signatures(Base64.decodeBase64(jsonPdf.getAsString("file")), Base64.decodeBase64(jsonPdf.getAsString("firma")));
            JSONObject jsonSignatures = signatures.getJsonSignatures();

            JSONObject jsonPdfSigned = new JSONObject();
            jsonPdfSigned.put("codigo",jsonPdf.get("codigo"));
            jsonPdfSigned.put("resultado",jsonSignatures);

            jsonArrayResult.add(jsonPdfSigned);
        }
        return jsonArrayResult;
    }
}
