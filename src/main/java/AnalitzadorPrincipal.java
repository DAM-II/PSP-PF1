import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
            CompletableFuture<Void> writeA = CompletableFuture.runAsync(() -> writeToProcess(processA, sampleLogs));
            CompletableFuture<Void> writeB = CompletableFuture.runAsync(() -> writeToProcess(processB, sampleLogs));

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

        } catch(IOException e){
            System.err.println("I/O error during process execution: " + e.getMessage());
        } catch(InterruptedException e){
            System.err.println("Process execution was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (CompletionException e) {
            System.err.println("Asynchronous stream communication error: " + e.getMessage());
        }
    }

    private static void writeToProcess(Process process, String data){
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))){
            writer.write(data);
            writer.flush();
        }catch (IOException e) {
            throw new CompletionException("Failed to write to child process", e);
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
            throw new CompletionException("Failed to read from child process", e);
        }
        return output.toString();
    }
}
