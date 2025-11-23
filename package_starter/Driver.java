import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Driver {

    public static void main(String[] args) throws IOException {
        /*for (int i = 1; i <= 5; i++){
            Path filePath = Path.of("./package_starter/input_given/syntax" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            System.out.println(Arrays.toString(lexicalAnalyser.getTokens()));

            Syntax syntaxAnalyser = new Syntax(lexicalAnalyser.getTokens());
            System.out.println(syntaxAnalyser.parse());
        }*/

        Path filePath = Path.of("./package_starter/input_given/syntax6.txt");
        String stream = Files.readString(filePath);

        Lex lexicalAnalyser = new Lex(stream);
        System.out.println(Arrays.toString(lexicalAnalyser.getTokens()) + "\n");

        Syntax syntaxAnalyser = new Syntax(lexicalAnalyser.getTokens());
        SyntaxNode syntax = syntaxAnalyser.parse();
        printNode(syntax);
        System.out.println("\n");
        printError(syntaxAnalyser.getErrors());

        Semantic semanticAnalyser = new Semantic(syntax);
        boolean val = semanticAnalyser.parse();
        System.out.println("\n" + val);
    }

    public static void printNode(SyntaxNode node){
        System.out.println(node + ", line = " + node.lineNumber + ", offset = " + node.characterOffset);
        for (SyntaxNode childNode : node.children) {
            if (childNode != null){
                System.out.println("    (" + childNode + ", line = " + childNode.lineNumber + ", offset = " + childNode.characterOffset + ")");
            }
        }
        for (SyntaxNode childNode : node.children) {
            if (childNode != null){
                printNode(childNode);
            }
        }
    }

    public static void printError(SyntaxError[] errors){
        for (SyntaxError error : errors){
            System.out.println(error.message);
        }
    }
}
