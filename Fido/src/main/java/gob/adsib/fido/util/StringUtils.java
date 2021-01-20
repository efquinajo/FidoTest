package gob.adsib.fido.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import org.codehaus.jackson.map.ObjectMapper;

import org.codehaus.jackson.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 *
 * @author MEFP
 */
public class StringUtils {
    public static Object toObject(String obj, Class clase) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(obj, clase);
        } catch (IOException ex) {
            throw new RuntimeException("Error en la conversion", ex);
        }

    }

    public static Object toObject(String obj, TypeReference tipo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(obj, tipo);
        } catch (IOException ex) {
            throw new RuntimeException("Error en la conversion", ex);
        }
    }
    
    public static String toJson(Object obj) {
        try {
            String theString = "";
            if (!(obj instanceof String)) {
                ObjectMapper mapper = new ObjectMapper();
                byte[] bytes = mapper.writeValueAsBytes(obj);
                InputStream resultadito = new ByteArrayInputStream(bytes);
                theString = StringUtils.read(resultadito);
            } else {
                theString = (String) obj;
            }
            return theString;
        } catch (Exception ex) {
            throw new RuntimeException("Error en la conversion", ex);
        }
    }
    
    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
