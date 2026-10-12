import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FiltreLog {
    public static void main(String[] args){
        String targetWord = "ERROR";
        int count = 0;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            String line;
            while((line = br.readLine()) != null){
                if(line.contains(targetWord)){
                    count++;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Num de Errors: " + count);
    }
}
