import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Driver {

    public static void main(String[] args) throws IOException {
        Driver driver = new Driver();
        driver.test();
    }

    public void test() throws IOException {
        for (int i = 1; i <= 6; i++){
            Path filePath = Path.of("./package_starter/input_given/semantic" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            Syntax syntaxAnalyser = new Syntax(lexicalAnalyser.getTokens());
            SyntaxNode tree = syntaxAnalyser.parse();
            Semantic semanticAnalyser = new Semantic(tree);
            System.out.println("File: semantic" + i + ", parsing: " + semanticAnalyser.parse() + "\n");
        }
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
