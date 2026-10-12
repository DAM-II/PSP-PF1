import java.io.*;

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
            File errorLog = new File("errors_filtre.log");
            pb.redirectError(errorLog);
            Process process = pb.start();

            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))){
                // writer.write(sampleLogs);
                writer.write("");
                writer.flush();
                writer.close();
            } // Closing try with resources automatically calls close

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;
                while((line = reader.readLine()) != null){
                    System.out.println("Hijo: " + line);
                }
            }

            process.waitFor();
            int exitCode = process.exitValue();

            if(exitCode == 0)
                System.out.println("Execution status: SUCCESS (Exit code 0)");
            else
                System.out.println("Execution status: FAILED (Exit code " + exitCode + ")");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
