import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class AnalitzadorPrincipal {
    public static void main(String[] args) {
        String sampleLogs = "INFO [Server] Service started\n" +
                "ERROR [Database] Connection timeout\n" +
                "WARN [Memory] High memory usage\n" +
                "ERROR [Auth] Invalid token\n" +
                "INFO [Server] Request processed\n";

        try{
            String javaBin = System.getProperty("java.home") + "/bin/java";
            String classPath = System.getProperty("java.class.path");
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classPath, "FiltreLog");
            Process process = pb.start();

            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))){
                writer.write(sampleLogs);
                writer.flush();
                writer.close();
            } // Closing try with resources automatically calls close

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;
                while((line = reader.readLine()) != null){
                    System.out.println("Hijo: " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Proceso de hijo terminado con codigo: " + exitCode);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
