package gob.adsib.fido.server;

import gob.adsib.fido.server.end_points.*;
import gob.adsib.fido.server.modules.ModuleManager;
import gob.adsib.fido.util.Encryption;
//import gob.adsib.fidomodulelectorhuellawin.RestCapturarHuella;
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
public class HttpsServerFido {

    private final Server server;
    private final int port;
    private ServletContextHandler servletHandler = new ServletContextHandler();

    public HttpsServerFido(int port) throws IOException {
        this.port = port;
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
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");
	
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler,servletHandler});
        server.setHandler(handlers);
        
        ModuleManager moduleManager = new ModuleManager(this);
        moduleManager.loadJarModules();
    }

    public ServletContextHandler getServletHandler() {
        return servletHandler;
    }

    private void loadEndPoints() {
        servletHandler.addServlet(RestTokenConectado.class, "/api/token/status");
        servletHandler.addServlet(RestObtenerDatosToken.class, "/api/token/data");
        servletHandler.addServlet(RestObtenerInfoToken.class, "/api/token/info");
        servletHandler.addServlet(RestGenerarParClaves.class, "/api/token/generate_keypar");
        servletHandler.addServlet(RestGenerarCSR.class, "/api/token/generate_csr");
        servletHandler.addServlet(RestCargarCertificado.class, "/api/token/cargar_pem");
        servletHandler.addServlet(RestFirmarSolicitudesToken.class, "/api/token/firmar_solicitudes");
        servletHandler.addServlet(RestFirmarPdfToken.class, "/api/token/firmar_pdf");
        servletHandler.addServlet(RestFirmarPdfLoteToken.class, "/api/token/firmar_lote_pdfs");
        servletHandler.addServlet(RestValidarFirmaPdf.class, "/api/validar_firma_pdf");
        servletHandler.addServlet(RestValidarFirmaMultiplesPdfs.class, "/api/validar_firma_multiples_pdfs");
        servletHandler.addServlet(RestServletStatus.class, "/api/status");
        servletHandler.addServlet(RestUsbDiskSerial.class, "/api/usbdisk/serial");
        servletHandler.addServlet(RestDetenerAplicacion.class, "/api/shutdown_service");
        servletHandler.addServlet(RestObtenerConfiguracion.class, "/api/config/obtener");
        servletHandler.addServlet(RestVerificarDriverToken.class, "/api/token/verificar_driver");
        servletHandler.addServlet(RestCrearPerfil.class, "/api/profile/crear");
        servletHandler.addServlet(RestObtenerPerfiles.class, "/api/profile/list");
        servletHandler.addServlet(RestCambiarPerfil.class, "/api/profile/change");
        servletHandler.addServlet(RestTokenConectadosV2.class, "/api/token/connected");
        servletHandler.addServlet(RestEliminarPerfil.class, "/api/profile/delete");
        servletHandler.addServlet(RestEliminarParDeClaves.class, "/api/token/eliminar_claves");
        servletHandler.addServlet(RestServletEnableDisableFirmatic.class, "/api/firmatic");
        servletHandler.addServlet(RestFirmarPdfStampToken.class, "/api/token/firmar_pdf_stamp");
        servletHandler.addServlet(RestFirmarPdfStampImgToken.class, "/api/token/firmar_pdf_stamp_img");

        // temporal para capturar ghuella, se deberia cargar como modulo
        //servletHandler.addServlet(RestCapturarHuella.class, "/api/huella/capturar");
        
        //Firmar estructura JSON ADSIB-MEFP
        servletHandler.addServlet(RestFirmarJsonToken.class, "/api/token/firmar_json");
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
