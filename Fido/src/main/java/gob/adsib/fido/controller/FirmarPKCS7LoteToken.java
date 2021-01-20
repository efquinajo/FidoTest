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
import java.security.Security;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author alberto
 */
public class FirmarPKCS7LoteToken {
    public static JSONArray firmarLotePKCS7(JSONObject jsonRequest) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        if (!jsonRequest.containsKey("slot")) {
            jsonRequest.appendField("slot", Config.DEFAULT_SLOT);
        }
        JSONArray jsonArrayFiles = (JSONArray) jsonRequest.get("files");
        String pin = jsonRequest.getAsString("pin");
        String alias = jsonRequest.getAsString("alias");
        long slot = Long.parseLong(jsonRequest.getAsString("slot"));
        Profile profile = Profile.getDefaultProfile();
        BaseKeyStore baseKeyStore = profile.getKeyStore();
        baseKeyStore.login(pin, slot);
        KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
        JSONArray jsonArrayFirmados = new JSONArray();
        for (Object object : jsonArrayFiles) {
            JSONObject jsonFile = (JSONObject) object;
            String file = jsonFile.getAsString("file");
            String id = jsonFile.getAsString("id");

            byte[] pdfSigned = keyAndCertificate.signPKCS7(Base64.decode(file));

            JSONObject jsonFirmado = new JSONObject();
            jsonFirmado.put("id", id);
            jsonFirmado.put("file", file);
            jsonFirmado.put("firma", new String(Base64.encode(pdfSigned)));
            jsonArrayFirmados.add(jsonFirmado);
        }
        baseKeyStore.logout();
        return jsonArrayFirmados;
    }
}
