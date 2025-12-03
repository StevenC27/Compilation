import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Driver {

    public static void main(String[] args) throws IOException {
        Driver driver = new Driver();
        driver.testLexical();
        driver.testSyntax();
        driver.testSemantic();
    }

    public void testLexical() throws IOException {
        for (int i = 1; i <= 6; i++){
            Path filePath = Path.of("./package_starter/input_given/lex" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            System.out.println("File: lex" + i + ",     Tokens: " + Arrays.toString(lexicalAnalyser.getTokens()));
            System.out.println("File: lex" + i + ",     Errors: " + Arrays.toString(lexicalAnalyser.getErrors()) + "\n");
        }
    }

    public void testSyntax() throws IOException{
        for (int i = 1; i <= 6; i++){
            Path filePath = Path.of("./package_starter/input_given/syntax" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            LexToken[] lexTokens = lexicalAnalyser.getTokens();
            LexError[] lexErrors = lexicalAnalyser.getErrors();
            System.out.println("File: syntax" + i + ",     LexTokens: " + Arrays.toString(lexTokens));
            System.out.println("File: syntax" + i + ",     LexErrors: " + Arrays.toString(lexErrors) + "\n");

            Syntax syntaxAnalyser = new Syntax(lexTokens);
            SyntaxNode tree = syntaxAnalyser.parse();
            SyntaxError[] syntaxErrors = syntaxAnalyser.getErrors();
            printNode(tree);
            System.out.println("\nFile: syntax" + i + ",     SyntaxErrors: " + Arrays.toString(syntaxErrors) + "\n");
        }
    }

    public void testSemantic() throws IOException {
        for (int i = 1; i <= 6; i++){
            Path filePath = Path.of("./package_starter/input_given/semantic" + i + ".txt");
            String stream = Files.readString(filePath);

            Lex lexicalAnalyser = new Lex(stream);
            LexToken[] lexTokens = lexicalAnalyser.getTokens();
            LexError[] lexErrors = lexicalAnalyser.getErrors();
            //System.out.println("File: semantic" + i + ", LexTokens: " + Arrays.toString(lexTokens));
            //System.out.println("File: semantic" + i + ", LexErrors: " + Arrays.toString(lexErrors) + "\n");

            Syntax syntaxAnalyser = new Syntax(lexTokens);
            SyntaxNode tree = syntaxAnalyser.parse();
            SyntaxError[] syntaxErrors = syntaxAnalyser.getErrors();
            //printNode(tree);
            //System.out.println("\nFile: semantic" + i + ", SyntaxErrors: " + Arrays.toString(syntaxErrors) + "\n");

            Semantic semanticAnalyser = new Semantic(tree);
            System.out.println("File: semantic" + i + ", parsing: " + semanticAnalyser.parse());
            System.out.println("File: semantic" + i + ", SemanticErrors: " + Arrays.toString(semanticAnalyser.getErrors()) + "\n");
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
