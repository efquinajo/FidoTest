package fidomoduleabstract;

import com.sun.net.httpserver.Headers;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

/**
 *
 * @author GIGABYTE
 */
public class HttpExchangeUtil {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Headers headersResponse = new Headers();

    public HttpExchangeUtil() {
    }

    public HttpServletResponse getResponse() {
        return response;
    }
    
    public static HttpExchangeUtil newInstance(HttpServletRequest request, HttpServletResponse response){
        HttpExchangeUtil httpExchangeUtil = new HttpExchangeUtil();
        httpExchangeUtil.request= request;
        httpExchangeUtil.response = response;
        return httpExchangeUtil;
    }
    
    public Headers getRequestHeaders (){
        Headers headers = new Headers();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                ArrayList<String> list = new ArrayList<>(1);
                list.add(request.getHeader(headerName));
                headers.put(headerName,list);
            }
        }
        return headers;
    }
    
    public HashMap<String,String> getRequestParams(){
        HashMap<String,String> map = new HashMap<String,String>();
        
        Enumeration<String> paramsNames = request.getParameterNames();

        if (paramsNames != null) {
            while (paramsNames.hasMoreElements()) {
                String paramName = paramsNames.nextElement();
                map.put(paramName,request.getParameter(paramName));
            }
        }
        return map;
    }

    public  URI getRequestURI (){
        return URI.create(request.getRequestURI());
    }

    public  String getRequestMethod (){
        return request.getMethod();
    }
    
    protected final JSONObject getRequestJSON(HttpExchangeUtil exchange) throws IOException, ParseException {
        InputStream inputStream = exchange.getRequestBody();
        Object object = new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(new InputStreamReader(inputStream, "UTF-8"));
        return (JSONObject) object;
    }

    public  InputStream getRequestBody () throws IOException{
        return request.getInputStream();
    }

    public  OutputStream getResponseBody () throws IOException{
        return response.getOutputStream();
    }

    public  void sendResponseHeaders (int rCode, long responseLength) throws IOException {
        response.setContentLengthLong(responseLength);
        response.setStatus(rCode);
    }
    
    public void close(){
        
    }

//    public  int getResponseCode (){
//        return 
//    }
//
//    public  void setStreams (InputStream i, OutputStream o){
//        
//    }
}