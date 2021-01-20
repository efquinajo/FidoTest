package gob.adsib.fido.stores.opensc;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import jnasmartcardio.Smartcardio;

/**
 * Clase utilitara que permite detectar si el token se encuentra conectado al
 * puerto USB
 *
 * @author Ronald Coarite
 */
public class Pkcs11Scan {

    private TerminalFactory context;

    /**
     * Constructor por defecto que inicializa y establece el proveedor de
     * seguridad para lectura de estados del token.
     */
    public Pkcs11Scan() {
        Provider provider = Security.getProvider(Smartcardio.PROVIDER_NAME);
        if (provider == null) {
            Security.addProvider(new Smartcardio());
        }
        try {
            context = TerminalFactory.getInstance("PC/SC", null, Smartcardio.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtien un array con id de los tokens conectados a los puertos USB.
     *
     * @return Un Array de Strign con los nombres o ID de los tokens conectados
     * @throws javax.smartcardio.CardException
     */
    public synchronized String[] getListConnected() throws CardException {
//            List<CardTerminal> terminals_list = context.terminals().list(State.CARD_INSERTION);
        List<CardTerminal> terminals_list = context.terminals().list();
        String cards[] = new String[terminals_list.size()];
        int i = 0;
        for (CardTerminal terminal : terminals_list) {
            cards[i++] = obtenerNombreToken(terminal.getName());
        }
        cards = Arrays.stream(cards).distinct().toArray(String[]::new);
        
        return cards;
    }

    private String obtenerNombreToken(String token) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(token, " ");
        if (tokenizer.hasMoreElements()) {
            builder.append(tokenizer.nextElement());
        }
        builder.append(" ");
        if (tokenizer.hasMoreElements()) {
            builder.append(tokenizer.nextElement());
        }
        return builder.toString();
    }

    public void close() {
        Provider provider = Security.getProvider(Smartcardio.PROVIDER_NAME);
        if (provider != null) {
            Security.removeProvider(Smartcardio.PROVIDER_NAME);
        }
    }
}
