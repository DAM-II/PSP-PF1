import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AnalitzadorPrincipal {
    public static void main(String[] args) {
        String sampleLogs = "INFO [Server] Service started\n" +
                "ERROR [Database] Connection timeout\n" +
                "WARNING [Memory] High memory usage\n" +
                "ERROR [Auth] Invalid token\n" +
                "WARNING [Disk] Free space below 10%\n" +
                "ERROR [Network] Gateway unreachable\n" +
                "WARNING [CPU] High core temperature\n";

        String javaBin = System.getProperty("java.home") + "/bin/java";
        String classPath = System.getProperty("java.class.path");
        File errorLog = new File("errors_filtre.log");

        try{
            // Process A: ERROR Filter
            ProcessBuilder pbA = new ProcessBuilder(javaBin, "-cp", classPath, "FiltreLog", "ERROR");
            pbA.redirectError(ProcessBuilder.Redirect.appendTo(errorLog));
            Process processA = pbA.start();

            ProcessBuilder pbB = new ProcessBuilder(javaBin, "-cp", classPath, "FiltreLog", "WARNING");
            pbB.redirectError(ProcessBuilder.Redirect.appendTo(errorLog));
            Process processB = pbB.start();

            // Feed both children concurrently using separate asynchronous tasks (prevents deadlocks)
            CompletableFuture<Void> writeA = CompletableFuture.runAsync(() -> {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(processA.getOutputStream()))){
                    writer.write(sampleLogs);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            CompletableFuture<Void> writeB = CompletableFuture.runAsync(() -> {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(processB.getOutputStream()))){
                    writer.write(sampleLogs);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Read child responses asynchronously to avoid blocking
            CompletableFuture<String> readA = CompletableFuture.supplyAsync(() -> readChildOutput(processA));
            CompletableFuture<String> readB = CompletableFuture.supplyAsync(() -> readChildOutput(processB));

            // Wait for all stream operations and both processes to finish
            CompletableFuture.allOf(writeA,writeB).join();
            processA.waitFor();
            processB.waitFor();

            int exitCodeA = processA.exitValue();
            int exitCodeB = processB.exitValue();

            int combinedExitCode = (exitCodeA == 0 && exitCodeB == 0) ? 0 : 1;

            String errorsCount = readA.join().trim();
            String warningsCount = readB.join().trim();

            System.out.println("RESULTAT: ERRORS=" + errorsCount + " | WARNINGS=" + warningsCount + " | EXIT_CODE=" + combinedExitCode);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String readChildOutput(Process process) {
        StringBuilder output = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            String line;
            while((line = reader.readLine()) != null){
                output.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
