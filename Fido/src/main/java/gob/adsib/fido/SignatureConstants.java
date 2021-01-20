package gob.adsib.fido;

/**
 * @author rcoarite
 */
public interface SignatureConstants
{
    
    // *********************************************************************************
    // ******  PARAMETROS DE MANEJO DE CERTIFICADOS Y LLAVES SEGUN LA ATT***************
    // *********************************************************************************
    
    /**
     * El tamaño de la firma
     */
    public static final int TAMANIO_DE_LLAVE = 2048;
    
    public static final String ALGORITMO_CLAVES = "RSA";
    
    public static final String ALGORITMO_HASH = "SHA256withRSA";
    
    
    /** 
     * Estandar de codificacion para las firmas
     */
    public static final String TIPO_DE_CERTIFICADO = "X.509";
    
    // *********************************************************************************
    // ******  PARAMETROS PARA LA GENERACION DE PAR DE CLAVES EN EL TOKEN **************
    // *********************************************************************************
    
    public final static int DEFAULT_SLOT_DATA_TOKEN = 0;
    
    public final static String X509_HEADER_TEXT="-----BEGIN CERTIFICATE-----";
    public final static String X509_FOOTER_TEXT="-----END CERTIFICATE-----";
}