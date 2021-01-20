package fidomoduleabstract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * @author ADSIB-UID
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //on class level
public  @interface  EndPoint {
    String path();
}