package gob.adsib.fido.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 *
 * @author GIGABYTE
 */
public class OsConsole {
//    public static void main(String cor[])throws Exception{
////        /usr/bin/pkcs11-tool
////        File file = buscarArchivo("/usr/bin","pkcs11-tool");
////        System.out.println("FILE: "+file.getAbsolutePath());
//        
//        String result = ejecutarComando(null);
//        System.out.println("RESULT: ");
//        System.out.println(result);
//    }
    
    public static String ejecutarComando(String command) throws Exception{
//        /usr/bin/pkcs11-tool --module opensc-pkcs11.so -l -O --pin 123456
//        String command = "/usr/bin/pkcs11-tool --module opensc-pkcs11.so -l -O --pin 123456";
		Runtime rt = Runtime.getRuntime();
		System.out.println("Comando: "+command);
		Process proc = rt.exec(command);

		InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		StringBuilder builder = new StringBuilder();
		while ((line = br.readLine()) != null)
			builder.append(line).append("\n");
		if (builder.length() > 0)
			builder.deleteCharAt(builder.length() - 1);
		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("Ocurrio un error al ejecutar el comando " + command);
		return builder.toString();
    }
    
    public static File buscarArchivo(String rootDir,String nameFile){
        @SuppressWarnings("Convert2Diamond")
        File rootDirFile = new File(rootDir);
        if(rootDirFile.isDirectory()){
            File files []= rootDirFile.listFiles();
            for (File file : files) {
                if(file.isDirectory()){
                    File fileTemp = buscarArchivo(file.getAbsolutePath(), nameFile);
                    if(fileTemp!=null)
                        return fileTemp;
                }else{
                    if(file.getName().contains(nameFile))
                        return file;
                }
            }
            return null;
        }else
        {
            if(rootDirFile.getName().equals(nameFile))
                return rootDirFile;
            else
                return null;
        }
    }
}