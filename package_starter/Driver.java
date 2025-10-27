import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Driver {

    public static void main(String[] args) throws IOException {
        Path filePath = Path.of("./package_starter/input_given/lex1.txt");
        String stream = Files.readString(filePath);


        Lex lexicalAnalyser = new Lex(stream);

        System.out.println(Arrays.toString(lexicalAnalyser.getTokens()));
    }
}
