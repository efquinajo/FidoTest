package gob.adsib.fido.server.end_points_firmatic;

import gob.adsib.fido.BaseKeyStore;
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.PkcsN11Manager;
import gob.adsib.fido.stores.profiles.AbstractInfo;
import gob.adsib.fido.stores.profiles.Pkcs11Profile;
import gob.adsib.fido.util.CertificateData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author GIGABYTE
 */
public class DialogSessionToken extends JFrame {
    private JComboBox<String> comboSlots;
    private JComboBox<CertificadoUsuario> comboCertificados;
    private JPasswordField passwordPin;
    private JButton btnLogin;
    private JButton btnFirmar;
    private JLabel txtMensajes;
    private Pkcs11Profile profile;
    private ArrayBlockingQueue<DataToken> blockingQueue;

    public DialogSessionToken(Pkcs11Profile profile) {
        super("Seleccione token");
        this.blockingQueue = new ArrayBlockingQueue<>(1);
        this.profile = profile;
        initComponents();
        pack();
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(450,50));
        
        ImageIcon imageIcon = new ImageIcon("/jfido.ico");
        setIconImage(imageIcon.getImage());
        
        // Panel Login
        JPanel contenedor = new JPanel(new CenterLayout());
        JPanel panelAuth= new JPanel();
        BoxLayout boxLayout = new BoxLayout(panelAuth,BoxLayout.Y_AXIS);
        panelAuth.setLayout(boxLayout);
        
        // Icono token
        panelAuth.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Combo slots
        JLabel txtMediosSeguridad = new JLabel("Medios de seguridad detectados");
        panelAuth.add(txtMediosSeguridad);
        JPanel panelConectados = new JPanel(new FlowLayout(FlowLayout.CENTER));
        comboSlots = new JComboBox<>();
        panelConectados.add(comboSlots);
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSlots();
            }
        });
        panelConectados.add(btnActualizar);
        panelAuth.add(panelConectados);
        
        // Espacio entre elementos
        panelAuth.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Contarseña token
        JPanel panelPin = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPin.add(new JLabel("PIN "));
        passwordPin = new JPasswordField(10);
        passwordPin.setEnabled(false);
        panelPin.add(passwordPin);
        // Boton login
        btnLogin = new JButton("Auntenticar");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onClickLogin(evt);
            }
        });
        btnLogin.setEnabled(false);
        passwordPin.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {warn();}
            @Override
            public void removeUpdate(DocumentEvent e) {warn();}
            @Override
            public void insertUpdate(DocumentEvent e) {warn();}
            public void warn() {
                btnLogin.setEnabled(passwordPin.getPassword().length>0);
            }
        });
        panelPin.add(btnLogin);
        panelPin.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelAuth.add(panelPin);
        
        
        
        JPanel panelCertificado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCertificado.add(new JLabel("Certificado: "));
        comboCertificados = new JComboBox<>();
        comboCertificados.setEnabled(false);
        panelCertificado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelCertificado.add(comboCertificados);
        panelAuth.add(panelCertificado);
        
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnFirmar = new JButton("Firmar");
        btnFirmar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onClickFirmar(evt);
            }
        });
        btnFirmar.setEnabled(false);
        panelControles.add(btnFirmar);
        panelAuth.add(panelControles);
       
        contenedor.add(panelAuth);
        add(contenedor, BorderLayout.CENTER);
        
        // Componentes para utilitarios
        txtMensajes = new JLabel();
        JPanel panelUtilitarios = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelUtilitarios.add(new JLabel("<html><body><strong>MENSAJE: </strong></body></html>"));
        panelUtilitarios.add(txtMensajes);
        add(panelUtilitarios, BorderLayout.SOUTH);
        
    }
    
    private void setMensaje(String mensaje){
        new Thread(new Runnable() {
            @Override
            public void run() {
                txtMensajes.setText(mensaje);
            }
        }).start();
    }
    
    private void onClickLogin(java.awt.event.ActionEvent evt) {
        JSONObject jsonToken = (JSONObject)comboSlots.getSelectedItem();
        setMensaje("Auntenticando...");
        
        String pin = new String(passwordPin.getPassword());
        long slot = Long.parseLong(String.valueOf(jsonToken.get("slot")));
        
        BaseKeyStore baseKeyStore = profile.getKeyStore();
        try {
            baseKeyStore.login(pin,slot);

            List<String> listAlias = baseKeyStore.getAlieces();

            DefaultComboBoxModel<CertificadoUsuario> model = new DefaultComboBoxModel<>();
            for (String alias: listAlias) {
                KeyAndCertificate keyAndCertificate = baseKeyStore.getKeyAndCertificate(alias);
                CertificateData certificateData = keyAndCertificate.getCertificateData();
                model.addElement(new CertificadoUsuario(keyAndCertificate, certificateData));
            }
            comboCertificados.setModel(model);
            comboCertificados.setEnabled(model.getSize()!=0);
            passwordPin.setEnabled(false);
            btnFirmar.setEnabled(true);
            
        } catch (Exception e) {
            setMensaje(e.getMessage());
        }
        finally{
            try {
                baseKeyStore.logout();
            } catch (Exception e) {}
        }
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                try {blockingQueue.put(null);} catch (Exception e) {}
            }
        });
        
//        String alias = String.valueOf(jsonToken.get("slot"));
    }
    
    private void onClickFirmar(java.awt.event.ActionEvent evt) {
        JSONObject jsonToken = (JSONObject)comboSlots.getSelectedItem();
        setMensaje("Iniciando firma...");
        String pin = new String(passwordPin.getPassword());
        long slot = Long.parseLong(String.valueOf(jsonToken.get("slot")));
        CertificadoUsuario certificadoUsuario = (CertificadoUsuario)comboCertificados.getSelectedItem();
        String alias = certificadoUsuario.keyAndCertificate.getAlias();
        
        try {
            blockingQueue.put(new DataToken(pin, alias, slot));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setVisible(false);
        dispose();
    }
    
    public class DataToken{
        private final String pin;
        private final String alias;
        private final long slot;

        public DataToken(String pin, String alias, long slot) {
            this.pin = pin;
            this.alias = alias;
            this.slot = slot;
        }

        public String getAlias() {
            return alias;
        }

        public String getPin() {
            return pin;
        }

        public long getSlot() {
            return slot;
        }
    }
    
    private class CertificadoUsuario {
        KeyAndCertificate keyAndCertificate;
        CertificateData certificateData;

        public CertificadoUsuario(KeyAndCertificate keyAndCertificate, CertificateData certificateData) {
            this.keyAndCertificate = keyAndCertificate;
            this.certificateData = certificateData;
        }

        @Override
        public String toString() {
            if(certificateData==null)
                return "";
            return certificateData.getSubjectName();
        }
    }
    
    public DataToken waitToCloseWindows(){
        try {
            return blockingQueue.take();
        } catch (Exception e) {
        }
        return null;
    }
    
    private JSONArray verificarDrivers(String pathDriver){
        JSONArray jsonDispositivos = new JSONArray();
        try {
            System.out.println("DRIVER: "+pathDriver);
            PkcsN11Manager n11Manager = new PkcsN11Manager(new File(pathDriver));
            long slots[] = n11Manager.getSlots();

            for (long slot : slots) {
                JSONObject jsonObject = new JSONObject();
                AbstractInfo info = n11Manager.getInfo(slot);
                jsonObject.put("slot", slot);
                jsonObject.put("globalName",info.getGlobalName());
                JSONObject tokenJson = new JSONObject();
                info.writeData(tokenJson);
                jsonObject.put("token",tokenJson);
                jsonDispositivos.add(jsonObject);
            }
            setMensaje("Validación de controlador realizada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            setMensaje(e.getMessage());
        }
        return jsonDispositivos;
    }
    
    public void loadSlots(){
        setMensaje("Buscando dispositivos conectados");
        new Thread(new Runnable() {
            @Override
            public void run() {
                setMensaje("Buscando dispositivos conectados...");
                try {
                    JSONArray jsonArray = verificarDrivers(profile.getPathDriver().getAbsolutePath());
                    if(jsonArray==null){
                        comboSlots.removeAllItems();
                        passwordPin.setEnabled(false);
                        setMensaje("No se detectaron dispositivos conectados");
                        return ;
                    }else if(jsonArray.isEmpty()){
                        comboSlots.removeAllItems();
                        passwordPin.setEnabled(false);
                        setMensaje("No se detectaron dispositivos conectados");
                        return ;
                    }else
                        passwordPin.setEnabled(true);
                    ComboBoxModel modelList = new DefaultComboBoxModel(jsonArray.toArray());
                    comboSlots.setRenderer(new CustomComboBoxRendererSlot());
                    comboSlots.setModel(modelList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private class CustomComboBoxRendererSlot extends JLabel implements ListCellRenderer {
        public CustomComboBoxRendererSlot() {
            setOpaque(true);
            setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
            setBackground(Color.BLUE);
            setForeground(Color.YELLOW);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            JSONObject jsonSlot = (JSONObject)value;
            JSONObject tokenJson = (JSONObject)jsonSlot.get("token");
            setText("<html>"+jsonSlot.get("globalName")+"<br/>Slot: "+jsonSlot.get("slot")+", Serial: "+tokenJson.get("serial")+"</html>");
            return this;
        }
    }
}