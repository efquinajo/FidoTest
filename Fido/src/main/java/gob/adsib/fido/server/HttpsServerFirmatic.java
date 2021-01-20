package gob.adsib.fido.server;

import gob.adsib.fido.server.end_points_firmatic.RestFirmarPdfLoteTokenFirmatic;
import gob.adsib.fido.server.end_points_firmatic.RestHomeFirmatic;
import gob.adsib.fido.util.Encryption;
import java.io.IOException;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 *
 * @author UID-ADSIB
 */
public class HttpsServerFirmatic {
    private final Server server;
    private final int port= 4637;
    private ServletContextHandler servletHandler = new ServletContextHandler();

    public HttpsServerFirmatic() throws IOException {
        server = new Server();

        final SslContextFactory sslContextFactory = new SslContextFactory("fido_keystore.jks");
        sslContextFactory.setKeyStorePassword(Encryption.decrypt("BCDEFGHI",10));
        final HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        final ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(this.port);
        server.addConnector(httpsConnector);
        
        loadEndPoints();
        
        // Cargando apidoc
//        ResourceHandler resource_handler = new ResourceHandler();
//        resource_handler.setDirectoriesListed(true);
//        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
//        resource_handler.setResourceBase(".");
	
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { servletHandler});
        server.setHandler(handlers);
    }
    
    private void loadEndPoints(){
        servletHandler.addServlet(RestFirmarPdfLoteTokenFirmatic.class, "/sign");
        servletHandler.addServlet(RestHomeFirmatic.class, "/");
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}