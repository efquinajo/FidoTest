package gob.adsib.fido.server.modules;

import fidomoduleabstract.EndPoint;
import gob.adsib.fido.server.HttpsServerFido;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author ADSIB-UID
 */
public class ModuleManager {
    private final String PATH_MODULES = "modules";
    private final String EXT_MODULE = ".jar";
    private final HttpsServerFido httpsServerFido;

    public ModuleManager(HttpsServerFido httsServerFido) {
        this.httpsServerFido = httsServerFido;
    }
    
    public void loadJarModules(){
        File folderModules = new File(PATH_MODULES);
        if(!folderModules.exists()){
            System.out.println("La carpeta que contiene los modulos no existe. Se omitira la carga de cualquier módulo");
            return;
        }
        System.out.println("RUTA_MODULOS: "+folderModules.getAbsolutePath());
        File[] jarModules = folderModules.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().lastIndexOf(EXT_MODULE)!=-1;
            }
        });
        System.out.println("Nro. módulos entontrados: "+jarModules.length);
        for (File jarModule : jarModules) {
            try {
                // Cargamos el JAR en tiempo de ejecución
                System.out.println("\tCargando JAR: "+jarModule.getAbsolutePath());
                loadJar(jarModule);
                System.out.println("\tjar cargado correctamente. Obteniendo rutas habilitadas en el Módulo");
                // Obtenemos todas las rutas a partir del JAR
                List<String> list = getClassFromJar(jarModule);
                loadEndPoints(list);
                // Carcamos 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private void loadJar(File jar) throws MalformedURLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        URL url = jar.toURI().toURL();

        URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }
    
    private List<String> getClassFromJar(File jar) throws FileNotFoundException, IOException{
        JarInputStream crunchifyJarFile = new JarInputStream(new FileInputStream(jar));
        JarEntry crunchifyJar;
        List<String> list = new LinkedList<>();
        while (true) {
            crunchifyJar = crunchifyJarFile.getNextJarEntry();
            if (crunchifyJar == null) {
                break;
            }
            if ((crunchifyJar.getName().endsWith(".class"))) {
                String className = crunchifyJar.getName().replaceAll("/", "\\.");
                String myClass = className.substring(0, className.lastIndexOf('.'));
                list.add(myClass);
            }
        }
        return list;
    }
    
    private void loadEndPoints(List<String> list) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException{
        for (String className : list) {
            Class<?> classCandidate = Class.forName(className);
            EndPoint endPoint = classCandidate.getAnnotation(EndPoint.class);
            if(endPoint != null){ // La clase es un 
                System.out.println("\t\tEnd Point: "+classCandidate.getName());
               
                httpsServerFido.getServletHandler().addServlet(classCandidate.getName(),endPoint.path());
            }
        }
    }
}