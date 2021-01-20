package fidomoduleabstract;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;

/**
 *
 * @author UID-ADSIB 2019
 */
public abstract class BaseEndPointServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException
    {
        HttpExchangeUtil exchangeUtil = HttpExchangeUtil.newInstance(request, response);
        handle(exchangeUtil);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpExchangeUtil exchangeUtil = HttpExchangeUtil.newInstance(request, response);
        handle(exchangeUtil);
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpExchangeUtil exchangeUtil = HttpExchangeUtil.newInstance(request, response);
        handle(exchangeUtil);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpExchangeUtil exchangeUtil = HttpExchangeUtil.newInstance(request, response);
        handle(exchangeUtil);
    }
    
    

    public final void handle(HttpExchangeUtil exchange) {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.getResponse().setHeader("Access-Control-Allow-Origin", "*");
            exchange.getResponse().setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponse().setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
            try {
                exchange.sendResponseHeaders(204, -1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exchange.close();
            }
            return;
        }
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        System.out.println(path);
        HashMap<String, String> parametros = exchange.getRequestParams();
        try {
            handleRequest(exchange, parametros);
            
        } catch (Exception e) {
            System.out.println("Error en servicio: " + exchange.getRequestURI().toString());
            e.printStackTrace();
        }
        finally{
            exchange.close();
        }
    }

    protected final JSONObject getRequestJSON(HttpExchangeUtil exchange) throws IOException, ParseException {
        InputStream inputStream = exchange.getRequestBody();
//        String data = IOUtils.readInputStreamToString(inputStream, Charset.forName("UTF-8"));
//        System.out.println("Request data: "+data);
//        Object object = new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(data);
        Object object = new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(new InputStreamReader(inputStream, "UTF-8"));
        return (JSONObject) object;
    }

    abstract protected void handleRequest(HttpExchangeUtil httpExchange, HashMap<String, String> parametros);

    protected final void writeErrorResponse(Exception ex, HttpExchangeUtil exchange) {
        System.out.println("Error en servicio");
        ex.printStackTrace(System.out);
        try {
            writeResponse(false, ex.getMessage(), null, 500, exchange);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final void writeError(Exception ex, HttpExchangeUtil exchange, Object datos) {
        ex.printStackTrace(System.out);
        try {
            writeResponse(false, ex.getMessage(), datos, 500, exchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final void writeResponse(boolean finalizado, String mensaje, Object datos, int httpStatus, HttpExchangeUtil exchange) throws IOException {
        JSONObject jsono = new JSONObject();
        jsono.put("finalizado", finalizado);
        jsono.put("mensaje", mensaje);
        jsono.put("datos", datos);

        byte[] resp = String.valueOf(jsono.toJSONString()).getBytes("UTF-8");
//        Headers responseHeaders = exchange.getResponseHeaders();

        exchange.getResponse().setHeader("Accept", "*/*");
        exchange.getResponse().setHeader("Accept-Ranges", "bytes");
        exchange.getResponse().setHeader("Access-Control-Allow-Origin", "*");
//        exchange.getResponse().setHeader("Access-Control-Credentials", "true" ); 
        exchange.getResponse().setHeader("Access-Control-Allow-Headers", "X-Requested-With");

        exchange.getResponse().setHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS");
        exchange.getResponse().setHeader("Access-Control-Max-Age", "1000");
        exchange.getResponse().setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponse().setHeader("Pragma", "no-cache");
        exchange.getResponse().setHeader("Expires", "0");
        exchange.getResponse().setHeader("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.getResponseBody().flush();
        exchange.close();
    }
}
