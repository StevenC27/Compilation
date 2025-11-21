import java.util.Map;

public class SemanticHelper {
    SyntaxNode syntax;
    Map<String, Integer> symbolTable;
    public SemanticHelper(SyntaxNode syntax){
        this.syntax = syntax;

    }

    /*
    *   navigate the syntax tree and check for any declarations.
    *   add the unknown declared variables to the symbol table.
    *
    *   create a stack for the scopes in the program.
    *   in these scopes store the variables that are available in the scope.
    *   this includes the variables from an inner scope.
    *
    *   check types
    * */

    public navigateTree(){
        return null;
    }
}
