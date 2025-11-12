import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Driver {

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++){
            Path filePath = Path.of("./package_starter/input_given/syntax" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            System.out.println(Arrays.toString(lexicalAnalyser.getTokens()));

            Syntax syntaxAnalyser = new Syntax(lexicalAnalyser.getTokens());
            System.out.println(syntaxAnalyser.parse());
        }
    }
}
