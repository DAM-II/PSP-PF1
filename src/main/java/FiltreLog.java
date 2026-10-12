import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FiltreLog {
    public static void main(String[] args){
        String targetWord = (args.length > 0 && !args[0].trim().isEmpty()) ? args[0].trim() : "ERROR";
        int count = 0;
        boolean hasContent = false;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            String line;
            while((line = br.readLine()) != null){
                if(!line.trim().isEmpty()){
                    hasContent = true;
                }
                if(line.contains(targetWord)){
                    count++;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        if(!hasContent){
            System.err.println("Error: Text Buit");
            System.exit(1);
        }

        System.out.println(count);
        System.exit(0);
    }
}
