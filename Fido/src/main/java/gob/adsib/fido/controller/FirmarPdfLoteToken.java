/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.adsib.fido.controller;

import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.Config;
import gob.adsib.fido.Profile;
import gob.adsib.fido.data.KeyAndCertificate;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author GIGABYTE
 */
public class FirmarPdfLoteToken {

    public static JSONArray firmarLotePDF(JSONObject jsonRequest) throws Exception {
        if (!jsonRequest.containsKey("slot")) {
            jsonRequest.appendField("slot", Config.DEFAULT_SLOT);
        }
//            System.out.println("JSON FIRMAR PDF's");
//            System.out.println(jsonRequest.toJSONString());
        JSONArray jsonArrayPdf = (JSONArray) jsonRequest.get("pdfs");
        String pin = jsonRequest.getAsString("pin");
        String alias = jsonRequest.getAsString("alias");
        long slot = Long.parseLong(jsonRequest.getAsString("slot"));

        Profile profile = Profile.getDefaultProfile();
        BaseKeyStore baseKeyStore = profile.getKeyStore();
        baseKeyStore.login(pin, slot);
        KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);

        JSONArray jsonArrayPdfFirmados = new JSONArray();
        for (Object object : jsonArrayPdf) {
            JSONObject jsonPdf = (JSONObject) object;
            String pdf = jsonPdf.getAsString("pdf");
            String id = jsonPdf.getAsString("id");

            byte[] pdfSigned = keyAndCertificate.signPdf(Base64.decode(pdf));

            JSONObject jsonPdfFirmado = new JSONObject();
            jsonPdfFirmado.put("id", id);
            jsonPdfFirmado.put("pdf_firmado", new String(Base64.encode(pdfSigned)));
            jsonArrayPdfFirmados.add(jsonPdfFirmado);
        }

        baseKeyStore.logout();

        return jsonArrayPdfFirmados;
    }
}
