package gob.adsib.fido.util;

//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author GIGABYTE
 */
public class Jws {
    public static String generate(RSAPublicKey publicKey, RSAPrivateKey privateKey,String subject,String urlCert){
////        Algorithm algorithm = Algorithm.RSA256(new );
//        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
//        Map<String,Object> map  = new HashMap<>();
//        map.put("x5u",urlCert);
//        
//        String token = JWT.create()
//            .withSubject(subject)
//            .withHeader(map)
//            .sign(algorithm);
//        return token;
return "";
    }
}
